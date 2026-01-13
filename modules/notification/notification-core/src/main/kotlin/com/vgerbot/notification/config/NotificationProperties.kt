package com.vgerbot.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 通知模块配置属性
 */
@ConfigurationProperties(prefix = "notification")
data class NotificationProperties(
    /**
     * 是否启用通知模块
     */
    var enabled: Boolean = true,
    
    /**
     * 默认提供商类型（用于自动选择提供商）
     */
    var defaultProvider: String? = null,
    
    /**
     * 是否记录通知发送历史
     */
    var recordHistory: Boolean = true,
    
    /**
     * 异步发送配置
     */
    var async: AsyncConfig = AsyncConfig(),
    
    /**
     * 重试配置
     */
    var retry: RetryConfig = RetryConfig()
)

/**
 * 异步发送配置
 */
data class AsyncConfig(
    /**
     * 是否启用异步发送
     */
    var enabled: Boolean = true,
    
    /**
     * 线程池核心线程数
     */
    var corePoolSize: Int = 5,
    
    /**
     * 线程池最大线程数
     */
    var maxPoolSize: Int = 10,
    
    /**
     * 队列容量
     */
    var queueCapacity: Int = 100
)

/**
 * 重试配置
 */
data class RetryConfig(
    /**
     * 是否启用重试
     */
    var enabled: Boolean = true,
    
    /**
     * 最大重试次数
     */
    var maxAttempts: Int = 3,
    
    /**
     * 重试间隔（毫秒）
     */
    var interval: Long = 1000
)

