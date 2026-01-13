package com.vgerbot.asynctask.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 异步任务配置属性
 */
@ConfigurationProperties(prefix = "async-task")
data class AsyncTaskProperties(
    /**
     * 消费者配置
     */
    val consumer: ConsumerProperties = ConsumerProperties()
) {
    data class ConsumerProperties(
        /**
         * 是否启用消费者
         */
        val enabled: Boolean = true,
        
        /**
         * 轮询间隔（秒）
         */
        val pollInterval: Int = 5,
        
        /**
         * 每次处理的任务数
         */
        val batchSize: Int = 10,
        
        /**
         * 并发处理线程数
         */
        val concurrency: Int = 3,
        
        /**
         * 最大处理时间（秒），超过此时间的 PROCESSING 状态任务将被重置为 PENDING
         */
        val maxProcessingTime: Int = 3600
    )
}

