package com.vgerbot.redis.service

import com.vgerbot.redis.config.RedisProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.TimeUnit

/**
 * Redis 缓存服务
 * 
 * 提供基于 Redis 的缓存功能，支持：
 * - 键值对缓存
 * - 过期时间设置
 * - 批量操作
 * - 缓存穿透保护
 * 
 * @param redisTemplate Redis 模板
 * @param redisProperties Redis 配置属性
 */
class RedisCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisProperties: RedisProperties
) {
    private val logger = LoggerFactory.getLogger(RedisCacheService::class.java)
    
    companion object {
        private const val CACHE_PREFIX = "cache:"
        private const val NULL_VALUE = "NULL"
    }
    
    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeoutSeconds 过期时间（秒），默认使用配置的默认值
     */
    fun set(key: String, value: Any?, timeoutSeconds: Long = redisProperties.cacheDefaultTimeout) {
        val fullKey = CACHE_PREFIX + key
        
        if (value == null) {
            // 防止缓存穿透：缓存 null 值，但使用较短的过期时间
            redisTemplate.opsForValue().set(fullKey, NULL_VALUE, 60, TimeUnit.SECONDS)
            logger.debug("Cached null value for key: {}", fullKey)
        } else {
            redisTemplate.opsForValue().set(fullKey, value, timeoutSeconds, TimeUnit.SECONDS)
            logger.debug("Cached value for key: {} with timeout: {} seconds", fullKey, timeoutSeconds)
        }
    }
    
    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值，如果不存在返回 null
     */
    fun get(key: String): Any? {
        val fullKey = CACHE_PREFIX + key
        val value = redisTemplate.opsForValue().get(fullKey)
        
        return if (value == NULL_VALUE) {
            // 返回 null 表示缓存了 null 值（防止缓存穿透）
            null
        } else {
            value
        }
    }
    
    /**
     * 获取缓存（带类型转换）
     * 
     * @param T 目标类型
     * @param key 缓存键
     * @return 缓存值，如果不存在返回 null
     */
    @Suppress("UNCHECKED_CAST")
    @JvmName("getTyped")
    inline fun <reified T> get(key: String): T? {
        val value = get(key)
        return value as? T
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return 是否成功删除
     */
    fun delete(key: String): Boolean {
        val fullKey = CACHE_PREFIX + key
        val deleted = redisTemplate.delete(fullKey) == true
        logger.debug("Deleted cache key: {}, result: {}", fullKey, deleted)
        return deleted
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键列表
     * @return 删除的数量
     */
    fun delete(keys: Collection<String>): Long {
        val fullKeys = keys.map { CACHE_PREFIX + it }
        val deleted = redisTemplate.delete(fullKeys.toSet()) ?: 0L
        logger.debug("Deleted {} cache keys", deleted)
        return deleted
    }
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    fun exists(key: String): Boolean {
        val fullKey = CACHE_PREFIX + key
        return redisTemplate.hasKey(fullKey) == true
    }
    
    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param timeoutSeconds 过期时间（秒）
     * @return 是否成功设置
     */
    fun expire(key: String, timeoutSeconds: Long): Boolean {
        val fullKey = CACHE_PREFIX + key
        val expired = redisTemplate.expire(fullKey, timeoutSeconds, TimeUnit.SECONDS) == true
        logger.debug("Set expire for key: {} to {} seconds, result: {}", fullKey, timeoutSeconds, expired)
        return expired
    }
    
    /**
     * 获取缓存剩余过期时间
     * 
     * @param key 缓存键
     * @return 剩余过期时间（秒），-1 表示永不过期，-2 表示键不存在
     */
    fun getExpire(key: String): Long {
        val fullKey = CACHE_PREFIX + key
        return redisTemplate.getExpire(fullKey, TimeUnit.SECONDS) ?: -2L
    }
    
    /**
     * 获取或设置缓存（如果不存在则执行操作并缓存结果）
     * 
     * @param key 缓存键
     * @param timeoutSeconds 过期时间（秒）
     * @param action 如果缓存不存在时执行的操作
     * @return 缓存值或操作结果
     */
    fun <T> getOrSet(key: String, timeoutSeconds: Long = redisProperties.cacheDefaultTimeout, action: () -> T): T {
        val cached = get(key) as T
        if (cached != null) {
            logger.debug("Cache hit for key: {}", key)
            return cached
        }
        
        logger.debug("Cache miss for key: {}, executing action", key)
        val value = action()
        set(key, value, timeoutSeconds)
        return value
    }
    
    /**
     * 清除所有缓存（谨慎使用）
     * 
     * @param pattern 匹配模式，例如 "user:*"，如果为 null 则清除所有缓存
     * @return 删除的数量
     */
    fun clear(pattern: String? = null): Long {
        val searchPattern = if (pattern != null) {
            CACHE_PREFIX + pattern
        } else {
            CACHE_PREFIX + "*"
        }
        
        val keys = redisTemplate.keys(searchPattern) ?: emptySet()
        if (keys.isEmpty()) {
            logger.debug("No keys found matching pattern: {}", searchPattern)
            return 0L
        }
        
        val deleted = redisTemplate.delete(keys) ?: 0L
        logger.info("Cleared {} cache keys matching pattern: {}", deleted, searchPattern)
        return deleted
    }
    
    /**
     * 递增缓存值
     * 
     * @param key 缓存键
     * @param delta 增量，默认为 1
     * @return 递增后的值
     */
    fun increment(key: String, delta: Long = 1): Long {
        val fullKey = CACHE_PREFIX + key
        val result = redisTemplate.opsForValue().increment(fullKey, delta) ?: 0L
        logger.debug("Incremented key: {} by {}, result: {}", fullKey, delta, result)
        return result
    }
    
    /**
     * 递减缓存值
     * 
     * @param key 缓存键
     * @param delta 减量，默认为 1
     * @return 递减后的值
     */
    fun decrement(key: String, delta: Long = 1): Long {
        return increment(key, -delta)
    }
}

