package com.vgerbot.asynctask.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.asynctask.config.AsyncTaskProperties
import com.vgerbot.asynctask.config.RetryStrategy
import com.vgerbot.asynctask.dao.AsyncTaskDao
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.asynctask.entity.AsyncTasks
import com.vgerbot.asynctask.entity.TaskStatus
import com.vgerbot.asynctask.processor.TaskProcessor
import com.vgerbot.asynctask.registry.TaskProcessorRegistry
import org.ktorm.dsl.eq
import com.vgerbot.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

/**
 * 异步任务消费者
 * 
 * 定时轮询数据库中的 PENDING 状态任务，并发处理它们
 */
@Component
@ConditionalOnProperty(prefix = "async-task.consumer", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AsyncTaskConsumer {
    
    private val logger = LoggerFactory.getLogger(AsyncTaskConsumer::class.java)
    
    @Autowired
    private lateinit var asyncTaskDao: AsyncTaskDao
    
    @Autowired
    private lateinit var taskProcessorRegistry: TaskProcessorRegistry
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var properties: AsyncTaskProperties
    
    @Autowired(required = false)
    private var notificationService: NotificationService? = null
    
    @Autowired(required = false)
    private var redisTemplate: StringRedisTemplate? = null
    
    private val executorService = Executors.newFixedThreadPool(
        properties.consumer.concurrency,
        { r -> Thread(r, "AsyncTaskConsumer-${r.hashCode()}") }
    )
    
    private val processingLock = ReentrantLock()
    private var isProcessing = false
    
    @PostConstruct
    fun init() {
        logger.info("异步任务消费者初始化完成: concurrency=${properties.consumer.concurrency}, batchSize=${properties.consumer.batchSize}")
    }
    
    @PreDestroy
    fun destroy() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }
        logger.info("异步任务消费者已关闭")
    }
    
    /**
     * 定时轮询并处理任务
     */
    @Scheduled(fixedDelayString = "\${async-task.consumer.poll-interval:5}", initialDelay = 10)
    fun pollAndProcessTasks() {
        // 防止并发执行
        if (!processingLock.tryLock()) {
            return
        }
        
        try {
            if (isProcessing) {
                return
            }
            isProcessing = true
            
            // 恢复超时的 PROCESSING 任务
            recoverTimeoutTasks()
            
            // 获取待处理任务
            val pendingTasks = asyncTaskDao.findPendingTasks(properties.consumer.batchSize)
            if (pendingTasks.isEmpty()) {
                return
            }
            
            logger.debug("找到 ${pendingTasks.size} 个待处理任务")
            
            // 并发处理任务
            val futures = pendingTasks.map { task ->
                CompletableFuture.runAsync({
                    processTask(task)
                }, executorService)
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(*futures.toTypedArray()).join()
            
        } finally {
            isProcessing = false
            processingLock.unlock()
        }
    }
    
    /**
     * 恢复超时的处理中任务
     */
    private fun recoverTimeoutTasks() {
        val timeoutTasks = asyncTaskDao.findTimeoutProcessingTasks(properties.consumer.maxProcessingTime)
        if (timeoutTasks.isNotEmpty()) {
            logger.warn("发现 ${timeoutTasks.size} 个超时的处理中任务，将重置为 PENDING 状态")
            timeoutTasks.forEach { task ->
                try {
                    task.taskStatus = TaskStatus.PENDING.name
                    task.startedAt = null
                    task.updatedAt = Instant.now()
                    asyncTaskDao.update(task)
                    logger.info("恢复超时任务: id=${task.id}")
                } catch (e: Exception) {
                    logger.error("恢复超时任务失败: id=${task.id}", e)
                }
            }
        }
    }
    
    /**
     * 处理单个任务
     */
    @Transactional
    fun processTask(task: AsyncTask) {
        val lockKey = "async_task_lock:${task.id}"
        val lockAcquired = tryAcquireDistributedLock(lockKey, 300) // 5分钟锁
        
        if (!lockAcquired) {
            logger.debug("任务 ${task.id} 正在被其他实例处理，跳过")
            return
        }
        
        try {
            // 重新加载任务，确保状态是最新的
            val currentTask = asyncTaskDao.findOne { AsyncTasks.id eq task.id } ?: return
            
            // 检查状态
            val currentStatus = TaskStatus.valueOf(currentTask.taskStatus)
            if (currentStatus != TaskStatus.PENDING && currentStatus != TaskStatus.RETRYING) {
                return
            }
            
            // 更新状态为 PROCESSING
            currentTask.taskStatus = TaskStatus.PROCESSING.name
            currentTask.startedAt = Instant.now()
            currentTask.updatedAt = Instant.now()
            asyncTaskDao.update(currentTask)
            
            // 获取处理器
            val processor = taskProcessorRegistry.getProcessor(currentTask.taskType)
            if (processor == null) {
                handleTaskFailure(currentTask, "找不到任务类型 '${currentTask.taskType}' 的处理器")
                return
            }
            
            // 反序列化 payload
            val payloadType = getPayloadType(processor)
            val payload = objectMapper.readValue(currentTask.payload, payloadType)
            
            // 执行任务
            val result = try {
                @Suppress("UNCHECKED_CAST")
                (processor as TaskProcessor<Any>).process(currentTask, payload)
            } catch (e: Exception) {
                logger.error("任务处理异常: id=${currentTask.id}, type=${currentTask.taskType}", e)
                com.vgerbot.asynctask.dto.TaskResult.failure("处理异常: ${e.message}")
            }
            
            // 处理结果
            if (result.success) {
                handleTaskSuccess(currentTask, result, processor)
            } else {
                handleTaskFailure(currentTask, result.message ?: "任务处理失败", result, processor)
            }
            
        } catch (e: Exception) {
            logger.error("处理任务失败: id=${task.id}", e)
            try {
                val currentTask = asyncTaskDao.findOne { AsyncTasks.id eq task.id }
                if (currentTask != null) {
                    handleTaskFailure(currentTask, "处理异常: ${e.message}")
                }
            } catch (ex: Exception) {
                logger.error("更新任务状态失败: id=${task.id}", ex)
            }
        } finally {
            releaseDistributedLock(lockKey)
        }
    }
    
    /**
     * 处理任务成功
     */
    private fun handleTaskSuccess(
        task: AsyncTask,
        result: com.vgerbot.asynctask.dto.TaskResult,
        processor: TaskProcessor<*>
    ) {
        task.taskStatus = TaskStatus.SUCCESS.name
        task.result = objectMapper.writeValueAsString(result.data)
        task.completedAt = Instant.now()
        task.updatedAt = Instant.now()
        asyncTaskDao.update(task)
        
        logger.info("任务处理成功: id=${task.id}, type=${task.taskType}")
        
        // 发送通知
        if (processor.shouldNotifyOnSuccess()) {
            sendNotification(task, result, processor)
        }
    }
    
    /**
     * 处理任务失败
     */
    private fun handleTaskFailure(
        task: AsyncTask,
        errorMessage: String,
        result: com.vgerbot.asynctask.dto.TaskResult? = null,
        processor: TaskProcessor<*>? = null
    ) {
        val canRetry = task.retryCount < task.maxRetryCount
        
        if (canRetry) {
            // 可以重试
            task.taskStatus = TaskStatus.RETRYING.name
            task.retryCount++
            task.errorMessage = errorMessage
            task.updatedAt = Instant.now()
            
            // 计算下次重试时间（这里简化处理，实际应该使用延迟队列）
            logger.info("任务将重试: id=${task.id}, retryCount=${task.retryCount}/${task.maxRetryCount}")
        } else {
            // 不能重试，标记为失败
            task.taskStatus = TaskStatus.FAILURE.name
            task.errorMessage = errorMessage
            task.result = result?.data?.let { objectMapper.writeValueAsString(it) }
            task.completedAt = Instant.now()
            task.updatedAt = Instant.now()
            
            logger.warn("任务处理失败: id=${task.id}, type=${task.taskType}, error=$errorMessage")
            
            // 发送通知
            if (processor?.shouldNotifyOnFailure() == true) {
                sendNotification(task, result ?: com.vgerbot.asynctask.dto.TaskResult.failure(errorMessage), processor)
            }
        }
        
        asyncTaskDao.update(task)
    }
    
    /**
     * 发送通知
     */
    private fun sendNotification(
        task: AsyncTask,
        result: com.vgerbot.asynctask.dto.TaskResult,
        processor: TaskProcessor<*>
    ) {
        try {
            val notificationRequest = processor.buildNotificationRequest(task, result)
            if (notificationRequest != null && notificationService != null) {
                notificationService!!.send(notificationRequest)
                logger.debug("发送任务通知: id=${task.id}")
            }
        } catch (e: Exception) {
            logger.error("发送任务通知失败: id=${task.id}", e)
        }
    }
    
    /**
     * 获取 payload 类型
     */
    private fun getPayloadType(processor: TaskProcessor<*>): Class<*> {
        val processorClass = processor.javaClass
        val typeArguments = processorClass.genericInterfaces
            .filterIsInstance<java.lang.reflect.ParameterizedType>()
            .firstOrNull { it.rawType == TaskProcessor::class.java }
            ?.actualTypeArguments
        
        return if (typeArguments != null && typeArguments.isNotEmpty()) {
            (typeArguments[0] as? Class<*>) ?: Any::class.java
        } else {
            Any::class.java
        }
    }
    
    /**
     * 尝试获取分布式锁
     */
    private fun tryAcquireDistributedLock(lockKey: String, timeoutSeconds: Int): Boolean {
        return if (redisTemplate != null) {
            try {
                val lockValue = "${Thread.currentThread().id}-${System.currentTimeMillis()}"
                val acquired = redisTemplate!!.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeoutSeconds.toLong(), TimeUnit.SECONDS)
                acquired == true
            } catch (e: Exception) {
                logger.warn("获取分布式锁失败，将使用本地处理: $lockKey", e)
                true // Redis 不可用时，允许本地处理
            }
        } else {
            true // 没有 Redis 时，允许本地处理
        }
    }
    
    /**
     * 释放分布式锁
     */
    private fun releaseDistributedLock(lockKey: String) {
        if (redisTemplate != null) {
            try {
                redisTemplate!!.delete(lockKey)
            } catch (e: Exception) {
                logger.warn("释放分布式锁失败: $lockKey", e)
            }
        }
    }
}

