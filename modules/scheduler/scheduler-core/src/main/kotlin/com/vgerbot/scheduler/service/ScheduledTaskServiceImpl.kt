package com.vgerbot.scheduler.service

import com.vgerbot.common.exception.BusinessException
import com.vgerbot.scheduler.dao.ScheduledTaskDao
import com.vgerbot.scheduler.dto.CreateScheduledTaskDto
import com.vgerbot.scheduler.dto.ScheduledTaskDto
import com.vgerbot.scheduler.dto.UpdateScheduledTaskDto
import com.vgerbot.scheduler.entity.ScheduledTask
import com.vgerbot.scheduler.entity.toDto
import com.vgerbot.scheduler.manager.TaskSchedulerManager
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ScheduledTaskServiceImpl : ScheduledTaskService {
    
    private val logger = LoggerFactory.getLogger(ScheduledTaskServiceImpl::class.java)
    
    @Autowired
    lateinit var scheduledTaskDao: ScheduledTaskDao
    
    @Autowired
    lateinit var taskSchedulerManager: TaskSchedulerManager
    
    @Transactional
    override fun createTask(dto: CreateScheduledTaskDto): ScheduledTaskDto? {
        // 检查任务名称是否已存在
        val existing = scheduledTaskDao.findByTaskName(dto.taskName)
        if (existing != null) {
            throw BusinessException("任务名称已存在: ${dto.taskName}")
        }
        
        // 验证cron表达式
        if (!isValidCronExpression(dto.cronExpression)) {
            throw BusinessException("无效的Cron表达式: ${dto.cronExpression}")
        }
        
        val task = ScheduledTask()
        task.taskName = dto.taskName
        task.taskType = dto.taskType
        task.executorType = dto.executorType
        task.cronExpression = dto.cronExpression
        task.taskConfig = dto.taskConfig
        task.description = dto.description
        task.enabled = dto.enabled
        task.runCount = 0
        task.successCount = 0
        task.failCount = 0
        task.status = 1
        task.createdAt = Instant.now()
        
        val result = scheduledTaskDao.add(task)
        if (result == 1) {
            val taskDto = task.toDto()
            // 如果任务启用，立即注册到调度器
            if (dto.enabled) {
                taskSchedulerManager.scheduleTask(task)
            }
            return taskDto
        }
        return null
    }
    
    @Transactional
    override fun updateTask(id: Long, dto: UpdateScheduledTaskDto): Boolean {
        val task = scheduledTaskDao.findOne { it.id eq id } ?: return false
        
        var cronChanged = false
        
        dto.taskName?.let {
            // 检查任务名称是否与其他任务冲突
            val existing = scheduledTaskDao.findByTaskName(it)
            if (existing != null && existing.id != id) {
                throw BusinessException("任务名称已存在: $it")
            }
            task.taskName = it
        }
        
        dto.taskType?.let { task.taskType = it }
        dto.executorType?.let { task.executorType = it }
        
        dto.cronExpression?.let {
            if (!isValidCronExpression(it)) {
                throw BusinessException("无效的Cron表达式: $it")
            }
            task.cronExpression = it
        }
        
        dto.taskConfig?.let { task.taskConfig = it }
        dto.description?.let { task.description = it }
        dto.enabled?.let { task.enabled = it }
        
        task.updatedAt = Instant.now()
        
        val updated = scheduledTaskDao.update(task) == 1
        if (updated) {
            // 如果任务启用，更新调度器
            if (task.enabled && task.status == 1) {
                taskSchedulerManager.updateTask(task)
            } else {
                // 如果任务停用，从调度器移除
                taskSchedulerManager.unscheduleTask(task.id)
            }
        }
        
        return updated
    }
    
    @Transactional
    override fun deleteTask(id: Long): Boolean {
        val task = scheduledTaskDao.findOne { it.id eq id } ?: return false
        
        // 从调度器移除
        taskSchedulerManager.unscheduleTask(id)
        
        // 逻辑删除
        task.status = 0
        task.updatedAt = Instant.now()
        return scheduledTaskDao.update(task) == 1
    }
    
    override fun getTaskById(id: Long): ScheduledTaskDto? {
        return scheduledTaskDao.findOne { (it.id eq id) and (it.status eq 1) }?.toDto()
    }
    
    override fun getAllTasks(): List<ScheduledTaskDto> {
        return scheduledTaskDao.findList { it.status eq 1 }.map { it.toDto() }
    }
    
    @Transactional
    override fun enableTask(id: Long): Boolean {
        val task = scheduledTaskDao.findOne { (it.id eq id) and (it.status eq 1) } ?: return false
        task.enabled = true
        task.updatedAt = Instant.now()
        val updated = scheduledTaskDao.update(task) == 1
        if (updated) {
            taskSchedulerManager.scheduleTask(task)
        }
        return updated
    }
    
    @Transactional
    override fun disableTask(id: Long): Boolean {
        val task = scheduledTaskDao.findOne { (it.id eq id) and (it.status eq 1) } ?: return false
        task.enabled = false
        task.updatedAt = Instant.now()
        val updated = scheduledTaskDao.update(task) == 1
        if (updated) {
            taskSchedulerManager.unscheduleTask(id)
        }
        return updated
    }
    
    override fun executeTask(id: Long): Boolean {
        val task = scheduledTaskDao.findOne { (it.id eq id) and (it.status eq 1) } ?: return false
        return taskSchedulerManager.executeTaskNow(task)
    }
    
    override fun reloadTasks() {
        taskSchedulerManager.reloadAllTasks()
    }
    
    /**
     * 验证Cron表达式
     */
    private fun isValidCronExpression(cron: String): Boolean {
        return try {
            org.springframework.scheduling.support.CronExpression.parse(cron)
            true
        } catch (e: Exception) {
            false
        }
    }
}

