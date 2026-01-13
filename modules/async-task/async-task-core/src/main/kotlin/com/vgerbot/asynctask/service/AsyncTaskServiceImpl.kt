package com.vgerbot.asynctask.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.asynctask.config.RetryConfig
import com.vgerbot.asynctask.config.RetryStrategy
import com.vgerbot.asynctask.dao.AsyncTaskDao
import com.vgerbot.asynctask.dto.CreateAsyncTaskDto
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.asynctask.entity.AsyncTasks
import com.vgerbot.asynctask.entity.TaskStatus
import com.vgerbot.asynctask.processor.TaskProcessor
import com.vgerbot.asynctask.registry.TaskProcessorRegistry
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import com.vgerbot.common.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AsyncTaskServiceImpl : AsyncTaskService {
    
    private val logger = LoggerFactory.getLogger(AsyncTaskServiceImpl::class.java)
    
    @Autowired
    private lateinit var asyncTaskDao: AsyncTaskDao
    
    @Autowired
    private lateinit var taskProcessorRegistry: TaskProcessorRegistry
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Transactional
    override fun <T : Any> submitTask(
        taskType: String,
        payload: T,
        priority: Int,
        retryConfig: RetryConfig?,
        tenantId: Int?
    ): AsyncTask {
        // 检查是否有对应的处理器
        if (!taskProcessorRegistry.hasProcessor(taskType)) {
            logger.warn("任务类型 '$taskType' 没有对应的处理器，任务将被创建但可能无法处理")
        }
        
        val processor = taskProcessorRegistry.getProcessor(taskType)
        @Suppress("UNCHECKED_CAST")
        val finalPriority = processor?.let { (it as TaskProcessor<Any>).getPriority(payload) } ?: priority
        val finalRetryConfig = processor?.getRetryConfig() ?: retryConfig
        
        val task = AsyncTask()
        task.taskType = taskType
        task.taskStatus = TaskStatus.PENDING.name
        task.priority = finalPriority
        task.payload = objectMapper.writeValueAsString(payload)
        task.retryCount = 0
        task.maxRetryCount = finalRetryConfig?.maxRetryCount ?: 0
        task.retryStrategy = finalRetryConfig?.strategy?.name
        task.retryInterval = finalRetryConfig?.baseInterval
        task.processorClass = processor?.javaClass?.name
        task.tenantId = tenantId
        task.statusFlag = 1  // 状态标志：1-正常，0-已取消
        task.isDeleted = false  // 不使用逻辑删除
        task.createdAt = Instant.now()
        
        asyncTaskDao.add(task)
        logger.info("创建异步任务: id=${task.id}, type=$taskType, priority=$finalPriority")
        
        return task
    }
    
    @Transactional
    override fun submitTask(dto: CreateAsyncTaskDto): AsyncTask {
        return submitTask(
            taskType = dto.taskType,
            payload = dto.payload,
            priority = dto.priority,
            retryConfig = dto.retryConfig,
            tenantId = dto.tenantId
        )
    }
    
    override fun getTask(id: Long): AsyncTask? {
        return asyncTaskDao.findOne { (AsyncTasks.id eq id) and (AsyncTasks.statusFlag eq 1) }
    }
    
    override fun getTasksByType(taskType: String?, status: TaskStatus?): List<AsyncTask> {
        return asyncTaskDao.findByTypeAndStatus(taskType, status)
    }
    
    @Transactional
    override fun cancelTask(id: Long): Boolean {
        val task = getTask(id) ?: return false
        
        val currentStatus = TaskStatus.valueOf(task.taskStatus)
        if (currentStatus != TaskStatus.PENDING && currentStatus != TaskStatus.RETRYING) {
            throw BusinessException("只能取消 PENDING 或 RETRYING 状态的任务")
        }
        
        task.statusFlag = 0  // 状态标志：0-已取消
        task.updatedAt = Instant.now()
        val updated = asyncTaskDao.update(task) == 1
        
        if (updated) {
            logger.info("取消任务: id=$id")
        }
        
        return updated
    }
    
    @Transactional
    override fun retryTask(id: Long): Boolean {
        val task = getTask(id) ?: return false
        
        val currentStatus = TaskStatus.valueOf(task.taskStatus)
        if (currentStatus != TaskStatus.FAILURE) {
            throw BusinessException("只能重试 FAILURE 状态的任务")
        }
        
        task.taskStatus = TaskStatus.PENDING.name
        task.retryCount = 0
        task.errorMessage = null
        task.updatedAt = Instant.now()
        val updated = asyncTaskDao.update(task) == 1
        
        if (updated) {
            logger.info("手动重试任务: id=$id")
        }
        
        return updated
    }
}

