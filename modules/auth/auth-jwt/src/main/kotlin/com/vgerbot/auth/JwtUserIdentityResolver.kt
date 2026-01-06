package com.vgerbot.auth

import com.vgerbot.common.security.UserIdentityResolver
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
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
class JwtUserIdentityResolver(
    private val jwtTokenUtils: JwtTokenUtils
) : UserIdentityResolver {
    
    private val logger = LoggerFactory.getLogger(JwtUserIdentityResolver::class.java)
    
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
    
    override fun resolveUserId(request: HttpServletRequest): Int? {
        // 优先从 Spring Security 上下文中获取
        getUserIdFromSecurityContext()?.let { return it }
        
        // 降级方案：从 JWT Token 中解析
        return getUserIdFromToken(request)
    }
    
    override fun resolveUsername(request: HttpServletRequest): String? {
        // 优先从 Spring Security 上下文中获取
        getUsernameFromSecurityContext()?.let { return it }
        
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
        
        // 尝试从 ExtendedUserDetails 获取用户 ID
        val principal = authentication.principal
        if (principal is ExtendedUserDetails) {
            return principal.userId
        }
        
        return null
    }
    
    /**
     * 从 Spring Security 上下文中获取用户名
     */
    private fun getUsernameFromSecurityContext(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        
        // 跳过匿名认证
        if (authentication.principal == "anonymousUser") {
            return null
        }
        
        return authentication.name
    }
    
    /**
     * 从 JWT Token 中获取用户 ID
     */
    private fun getUserIdFromToken(request: HttpServletRequest): Int? {
        val token = extractToken(request) ?: return null
        
        return try {
            jwtTokenUtils.getUserIdFromToken(token)
        } catch (e: Exception) {
            logger.debug("Failed to get user ID from token: ${e.message}")
            null
        }
    }
    
    /**
     * 从 Authorization 头中解析 JWT Token 获取用户名
     */
    private fun getUsernameFromToken(request: HttpServletRequest): String? {
        val token = extractToken(request) ?: return null
        
        return try {
            jwtTokenUtils.getUsernameFromToken(token)
        } catch (e: Exception) {
            logger.debug("Failed to get username from token: ${e.message}")
            null
        }
    }
    
    /**
     * 从请求头提取 Token
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null
        }
        return authHeader.substring(BEARER_PREFIX.length).trim()
    }
}
