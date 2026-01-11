package com.vgerbot.redis.service

import com.vgerbot.redis.config.RedisProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Redis 分布式锁服务
 * 
 * 提供基于 Redis 的分布式锁功能，支持：
 * - 可重入锁
 * - 自动续期
 * - 超时释放
 * 
 * @param redisTemplate Redis 模板
 * @param redisProperties Redis 配置属性
 */
class RedisLockService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisProperties: RedisProperties
) {
    private val logger = LoggerFactory.getLogger(RedisLockService::class.java)
    
    companion object {
        private const val LOCK_PREFIX = "lock:"
        private const val LOCK_VALUE_PREFIX = "locked:"
        
        // Lua 脚本：释放锁（只有锁的持有者才能释放）
        private val UNLOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
        """.trimIndent()
        
        // Lua 脚本：续期锁
        private val RENEW_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("pexpire", KEYS[1], ARGV[2])
            else
                return 0
            end
        """.trimIndent()
    }
    
    private val unlockScript = DefaultRedisScript<Long>(UNLOCK_SCRIPT, Long::class.java)
    private val renewScript = DefaultRedisScript<Long>(RENEW_SCRIPT, Long::class.java)
    
    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁的键
     * @param timeoutSeconds 锁的超时时间（秒），默认使用配置的默认值
     * @return 锁的标识符，如果获取失败返回 null
     */
    fun tryLock(lockKey: String, timeoutSeconds: Long = redisProperties.lockDefaultTimeout): String? {
        val fullKey = LOCK_PREFIX + lockKey
        val lockValue = LOCK_VALUE_PREFIX + UUID.randomUUID().toString()
        
        val acquired = redisTemplate.opsForValue().setIfAbsent(
            fullKey,
            lockValue,
            timeoutSeconds,
            TimeUnit.SECONDS
        )
        
        return if (acquired == true) {
            logger.debug("Lock acquired: {}", fullKey)
            lockValue
        } else {
            logger.debug("Failed to acquire lock: {}", fullKey)
            null
        }
    }
    
    /**
     * 获取分布式锁（阻塞直到成功或超时）
     * 
     * @param lockKey 锁的键
     * @param timeoutSeconds 锁的超时时间（秒）
     * @param waitTimeoutSeconds 等待获取锁的超时时间（秒），默认 10 秒
     * @return 锁的标识符，如果获取失败返回 null
     */
    fun lock(
        lockKey: String,
        timeoutSeconds: Long = redisProperties.lockDefaultTimeout,
        waitTimeoutSeconds: Long = 10
    ): String? {
        val startTime = System.currentTimeMillis()
        val waitTimeoutMillis = waitTimeoutSeconds * 1000
        
        while (System.currentTimeMillis() - startTime < waitTimeoutMillis) {
            val lockValue = tryLock(lockKey, timeoutSeconds)
            if (lockValue != null) {
                return lockValue
            }
            
            try {
                Thread.sleep(redisProperties.lockRetryInterval)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return null
            }
        }
        
        logger.warn("Failed to acquire lock within {} seconds: {}", waitTimeoutSeconds, lockKey)
        return null
    }
    
    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的标识符（从 tryLock 或 lock 方法返回）
     * @return 是否成功释放
     */
    fun unlock(lockKey: String, lockValue: String): Boolean {
        val fullKey = LOCK_PREFIX + lockKey
        
        val result = redisTemplate.execute(unlockScript, listOf(fullKey), lockValue)
        val released = result == 1L
        
        if (released) {
            logger.debug("Lock released: {}", fullKey)
        } else {
            logger.warn("Failed to release lock (may not be the owner): {}", fullKey)
        }
        
        return released
    }
    
    /**
     * 续期分布式锁
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的标识符
     * @param timeoutSeconds 新的超时时间（秒）
     * @return 是否成功续期
     */
    fun renew(lockKey: String, lockValue: String, timeoutSeconds: Long = redisProperties.lockDefaultTimeout): Boolean {
        val fullKey = LOCK_PREFIX + lockKey
        val timeoutMillis = timeoutSeconds * 1000
        
        val result = redisTemplate.execute(renewScript, listOf(fullKey), lockValue, timeoutMillis.toString())
        val renewed = result == 1L
        
        if (renewed) {
            logger.debug("Lock renewed: {} for {} seconds", fullKey, timeoutSeconds)
        } else {
            logger.warn("Failed to renew lock (may not be the owner): {}", fullKey)
        }
        
        return renewed
    }
    
    /**
     * 执行带锁的操作
     * 
     * @param lockKey 锁的键
     * @param timeoutSeconds 锁的超时时间（秒）
     * @param action 要执行的操作
     * @return 操作的结果，如果获取锁失败返回 null
     */
    fun <T> executeWithLock(lockKey: String, timeoutSeconds: Long = redisProperties.lockDefaultTimeout, action: () -> T): T? {
        val lockValue = lock(lockKey, timeoutSeconds) ?: return null
        
        return try {
            action()
        } finally {
            unlock(lockKey, lockValue)
        }
    }
    
    /**
     * 检查锁是否存在
     * 
     * @param lockKey 锁的键
     * @return 锁是否存在
     */
    fun isLocked(lockKey: String): Boolean {
        val fullKey = LOCK_PREFIX + lockKey
        return redisTemplate.hasKey(fullKey) == true
    }
}

