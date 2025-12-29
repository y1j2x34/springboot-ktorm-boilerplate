package com.vgerbot.auth

import com.vgerbot.common.security.UserIdentityResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest

/**
 * 基于 JWT 的用户身份解析器
 * 
 * 优先从 Spring Security 上下文中获取用户信息
 * 如果上下文中没有，则尝试从 Authorization 头中解析 JWT Token
 */
@Component
class JwtUserIdentityResolver : UserIdentityResolver {
    
    private val logger = LoggerFactory.getLogger(JwtUserIdentityResolver::class.java)
    
    @Autowired
    private lateinit var jwtTokenUtils: JwtTokenUtils
    
    override fun resolveUserId(request: HttpServletRequest): Int? {
        // 优先从 Spring Security 上下文中获取
        val userIdFromContext = getUserIdFromSecurityContext()
        if (userIdFromContext != null) {
            return userIdFromContext
        }
        
        // 降级方案：从 JWT Token 中解析
        val username = resolveUsername(request) ?: return null
        
        // 假设用户名就是用户ID（根据实际情况调整）
        // 如果需要从数据库查询，建议在这里注入 UserService
        return username.toIntOrNull()
    }
    
    override fun resolveUsername(request: HttpServletRequest): String? {
        // 优先从 Spring Security 上下文中获取
        val usernameFromContext = getUsernameFromSecurityContext()
        if (usernameFromContext != null) {
            return usernameFromContext
        }
        
        // 降级方案：从 Authorization 头中解析
        return getUsernameFromToken(request)
    }
    
    /**
     * 从 Spring Security 上下文中获取用户ID
     */
    private fun getUserIdFromSecurityContext(): Int? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        
        val username = authentication.name ?: return null
        return username.toIntOrNull()
    }
    
    /**
     * 从 Spring Security 上下文中获取用户名
     */
    private fun getUsernameFromSecurityContext(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        
        return authentication.name
    }
    
    /**
     * 从 Authorization 头中解析 JWT Token 获取用户名
     */
    private fun getUsernameFromToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null
        }
        
        val token = authHeader.substring(7)
        
        return try {
            jwtTokenUtils.getUsernameFromToken(token)
        } catch (e: Exception) {
            logger.error("解析 JWT Token 失败: ${e.message}")
            null
        }
    }
}

