package com.vgerbot.redis.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Redis 配置属性
 * 
 * 用于配置 Redis 连接和默认参数
 */
@ConfigurationProperties(prefix = "vgerbot.redis")
data class RedisProperties(
    /**
     * 分布式锁默认过期时间（秒）
     */
    val lockDefaultTimeout: Long = 30,
    
    /**
     * 缓存默认过期时间（秒）
     */
    val cacheDefaultTimeout: Long = 3600,
    
    /**
     * 分布式锁获取失败时的重试间隔（毫秒）
     */
    val lockRetryInterval: Long = 100,
    
    /**
     * 分布式锁最大重试次数
     */
    val lockMaxRetries: Int = 10
)

