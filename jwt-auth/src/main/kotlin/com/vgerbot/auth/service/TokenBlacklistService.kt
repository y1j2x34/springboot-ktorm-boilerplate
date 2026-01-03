package com.vgerbot.auth.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.Date

/**
 * Token 黑名单服务
 * 
 * 用于管理已撤销的 Token（如登出、密码修改等场景）
 * 
 * 注意：当前实现使用内存存储，仅适用于单实例部署。
 * 生产环境建议使用 Redis 等分布式缓存实现。
 */
@Service
class TokenBlacklistService {
    
    /**
     * 黑名单存储：Token ID (jti) -> 过期时间
     * 过期后自动清理
     */
    private val blacklist = ConcurrentHashMap<String, Date>()
    
    /**
     * 将 Token 加入黑名单
     * 
     * @param tokenId Token 的唯一标识 (jti claim)
     * @param expiration Token 的过期时间，用于自动清理
     */
    fun addToBlacklist(tokenId: String, expiration: Date) {
        blacklist[tokenId] = expiration
        // 触发清理过期条目
        cleanupExpired()
    }
    
    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param tokenId Token 的唯一标识 (jti claim)
     * @return true 如果在黑名单中，false 否则
     */
    fun isBlacklisted(tokenId: String): Boolean {
        val expiration = blacklist[tokenId] ?: return false
        // 如果已过期，从黑名单移除
        if (expiration.before(Date())) {
            blacklist.remove(tokenId)
            return false
        }
        return true
    }
    
    /**
     * 从黑名单移除 Token
     */
    fun removeFromBlacklist(tokenId: String) {
        blacklist.remove(tokenId)
    }
    
    /**
     * 清理已过期的黑名单条目
     */
    private fun cleanupExpired() {
        val now = Date()
        blacklist.entries.removeIf { it.value.before(now) }
    }
    
    /**
     * 获取黑名单大小（用于监控）
     */
    fun size(): Int = blacklist.size
    
    /**
     * 清空黑名单（慎用）
     */
    fun clear() {
        blacklist.clear()
    }
}

