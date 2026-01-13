package com.vgerbot.asynctask.registry

import com.vgerbot.asynctask.processor.TaskProcessor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

/**
 * 任务处理器注册表
 * 
 * 自动发现并注册所有 TaskProcessor 实现
 */
@Component
class TaskProcessorRegistry {
    
    private val logger = LoggerFactory.getLogger(TaskProcessorRegistry::class.java)
    
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    
    private val processors = mutableMapOf<String, TaskProcessor<*>>()
    
    @PostConstruct
    fun init() {
        val processorBeans = applicationContext.getBeansOfType(TaskProcessor::class.java)
        processorBeans.values.forEach { processor ->
            val taskType = processor.getTaskType()
            if (processors.containsKey(taskType)) {
                logger.warn("任务类型 '$taskType' 有多个处理器，将使用最后注册的: ${processor.javaClass.name}")
            }
            processors[taskType] = processor
            logger.info("注册任务处理器: $taskType -> ${processor.javaClass.name}")
        }
        logger.info("共注册 ${processors.size} 个任务处理器")
    }
    
    /**
     * 获取指定类型的处理器
     * 
     * @param taskType 任务类型
     * @return 处理器，如果不存在则返回 null
     */
    fun getProcessor(taskType: String): TaskProcessor<*>? {
        return processors[taskType]
    }
    
    /**
     * 检查是否存在指定类型的处理器
     */
    fun hasProcessor(taskType: String): Boolean {
        return processors.containsKey(taskType)
    }
    
    /**
     * 获取所有已注册的任务类型
     */
    fun getAllTaskTypes(): Set<String> {
        return processors.keys.toSet()
    }
}

