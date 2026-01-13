package com.vgerbot.scheduler.manager

import com.vgerbot.scheduler.dao.ScheduledTaskDao
import com.vgerbot.scheduler.entity.ScheduledTask
import com.vgerbot.scheduler.executor.TaskExecutor
import com.vgerbot.scheduler.executor.TaskExecutionResult
import com.vgerbot.scheduler.executor.impl.SchedulingHealthChecker
import com.vgerbot.scheduler.executor.impl.SchedulingJavaScriptRunner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronExpression
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import org.ktorm.dsl.eq

/**
 * 任务调度管理器
 * 负责动态管理Spring Scheduling任务
 */
@Component
class TaskSchedulerManager(
    val taskScheduler: TaskScheduler,
    val scheduledTaskDao: ScheduledTaskDao,
    val schedulingJavaScriptRunner: SchedulingJavaScriptRunner,
    val schedulingHealthChecker: SchedulingHealthChecker
) {
    
    private val logger = LoggerFactory.getLogger(TaskSchedulerManager::class.java)

    // 存储任务ID到ScheduledFuture的映射
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    
    // 存储执行器实例
    private val executorRegistry = ConcurrentHashMap<String, TaskExecutor>()
    
    init {
        // 注册内置执行器
        registerExecutor(schedulingJavaScriptRunner)
        registerExecutor(schedulingHealthChecker)
    }
    
    /**
     * 注册执行器
     */
    fun registerExecutor(executor: TaskExecutor) {
        executorRegistry[executor.getExecutorType()] = executor
        logger.info("注册任务执行器: {}", executor.getExecutorType())
    }
    
    /**
     * 获取执行器
     */
    private fun getExecutor(task: ScheduledTask): TaskExecutor? {
        return when (task.taskType) {
            "BUILTIN" -> executorRegistry[task.executorType]
            "CLASS" -> {
                try {
                    val clazz = Class.forName(task.executorType)
                    clazz.getDeclaredConstructor().newInstance() as? TaskExecutor
                } catch (e: Exception) {
                    logger.error("无法加载执行器类: ${task.executorType}", e)
                    null
                }
            }
            else -> null
        }
    }
    
    /**
     * 调度任务
     */
    fun scheduleTask(task: ScheduledTask) {
        if (!task.enabled || task.status != 1) {
            logger.warn("任务未启用或已停用，跳过调度: {}", task.taskName)
            return
        }
        
        // 如果任务已存在，先取消
        unscheduleTask(task.id)
        
        try {
            val cronExpression = CronExpression.parse(task.cronExpression)
            val executor = getExecutor(task)
            
            if (executor == null) {
                logger.error("无法找到执行器: ${task.executorType}")
                return
            }
            
            val taskRunnable = Runnable {
                executeTask(task, executor)
            }
            
            // 计算下次执行时间
            // 优先使用数据库中保存的 nextRunTime（如果存在且未过期），以保持执行时间的连续性
            val now = Instant.now()
            val nextRunTime = if (task.nextRunTime != null && task.nextRunTime!!.isAfter(now)) {
                // 使用数据库中保存的未过期的执行时间（服务重启时保持原有执行计划）
                logger.debug("任务 {} 使用数据库中保存的执行时间: {}", task.taskName, task.nextRunTime)
                task.nextRunTime!!
            } else {
                // 基于当前时间计算新的执行时间（首次创建或执行时间已过期）
                val calculated = cronExpression.next(now)
                if (task.nextRunTime != null && task.nextRunTime!!.isBefore(now)) {
                    logger.info("任务 {} 的执行时间已过期（原: {}），重新计算: {}", task.taskName, task.nextRunTime, calculated)
                }
                calculated
            }
            task.nextRunTime = nextRunTime
            
            // 保存下次执行时间到数据库
            scheduledTaskDao.update(task)
            
            // 使用CronTrigger调度任务
            // 注意：CronTrigger 会根据 cron 表达式自动计算执行时间，通常与 nextRunTime 一致
            val cronTrigger = CronTrigger(task.cronExpression)
            val scheduledFuture: ScheduledFuture<*>? = taskScheduler.schedule(taskRunnable, cronTrigger)
            
            scheduledFuture?.let {
                scheduledTasks[task.id] = it
            }
            logger.info("任务已调度: {} (ID: {}), 下次执行时间: {}", task.taskName, task.id, nextRunTime)
        } catch (e: Exception) {
            logger.error("调度任务失败: ${task.taskName}", e)
        }
    }
    
    /**
     * 更新任务
     */
    fun updateTask(task: ScheduledTask) {
        scheduleTask(task)
    }
    
    /**
     * 取消任务调度
     */
    fun unscheduleTask(taskId: Long) {
        scheduledTasks[taskId]?.let { future ->
            future.cancel(false)
            scheduledTasks.remove(taskId)
            logger.info("任务已取消调度: {}", taskId)
        }
    }
    
    /**
     * 执行任务
     */
    @Transactional
    fun executeTask(task: ScheduledTask, executor: TaskExecutor): TaskExecutionResult {
        val startTime = Instant.now()
        
        // 重新从数据库加载任务，确保获取最新配置
        val currentTask = scheduledTaskDao.findOne { it.id eq task.id } ?: task
        
        try {
            // 更新任务状态为运行中
            currentTask.lastRunStatus = "RUNNING"
            currentTask.lastRunTime = startTime
            scheduledTaskDao.update(currentTask)
            
            // 执行任务
            val result = executor.execute(currentTask.taskConfig)
            
            // 更新任务执行结果
            currentTask.runCount++
            if (result.success) {
                currentTask.successCount++
                currentTask.lastRunStatus = "SUCCESS"
            } else {
                currentTask.failCount++
                currentTask.lastRunStatus = "FAILED"
            }
            currentTask.lastRunMessage = result.message
            currentTask.lastRunTime = Instant.now()
            
            // 计算下次执行时间
            val cronExpression = CronExpression.parse(currentTask.cronExpression)
            currentTask.nextRunTime = cronExpression.next(Instant.now())
            
            scheduledTaskDao.update(currentTask)
            
            logger.info("任务执行完成: {} (ID: {}), 成功: {}", currentTask.taskName, currentTask.id, result.success)
            return result
        } catch (e: Exception) {
            logger.error("任务执行异常: {} (ID: {})", currentTask.taskName, currentTask.id, e)
            
            // 更新任务执行结果
            currentTask.runCount++
            currentTask.failCount++
            currentTask.lastRunStatus = "FAILED"
            currentTask.lastRunMessage = "执行异常: ${e.message}"
            currentTask.lastRunTime = Instant.now()
            
            try {
                scheduledTaskDao.update(currentTask)
            } catch (updateException: Exception) {
                logger.error("更新任务状态失败", updateException)
            }
            
            return TaskExecutionResult(
                success = false,
                message = "执行异常: ${e.message}"
            )
        }
    }
    
    /**
     * 立即执行任务
     */
    fun executeTaskNow(task: ScheduledTask): Boolean {
        val executor = getExecutor(task) ?: return false
        
        return try {
            val result = executeTask(task, executor)
            result.success
        } catch (e: Exception) {
            logger.error("立即执行任务失败: ${task.taskName}", e)
            false
        }
    }
    
    /**
     * 重新加载所有任务
     */
    fun reloadAllTasks() {
        logger.info("重新加载所有任务")
        
        // 取消所有现有任务
        scheduledTasks.keys.toList().forEach { taskId ->
            unscheduleTask(taskId)
        }
        
        // 加载所有启用的任务
        val tasks = scheduledTaskDao.findAllEnabled()
        tasks.forEach { task ->
            scheduleTask(task)
        }
        
        logger.info("已重新加载 {} 个任务", tasks.size)
    }
}

