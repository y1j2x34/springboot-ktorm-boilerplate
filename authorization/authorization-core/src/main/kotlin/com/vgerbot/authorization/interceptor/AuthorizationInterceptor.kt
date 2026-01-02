package com.vgerbot.authorization.interceptor

import com.vgerbot.authorization.annotation.RequiresPermission
import com.vgerbot.authorization.annotation.RequiresRole
import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.authorization.config.AuthorizationProperties
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 授权拦截器
 * 拦截带有 @RequiresPermission 和 @RequiresRole 注解的方法
 * 在方法执行前进行权限检查
 */
@Component
class AuthorizationInterceptor(
    private val authorizationService: AuthorizationService,
    private val properties: AuthorizationProperties
) : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(AuthorizationInterceptor::class.java)
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!properties.enabled) {
            return true
        }
        
        // 只处理 HandlerMethod
        if (handler !is HandlerMethod) {
            return true
        }
        
        // 获取当前用户
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            logger.debug("No authenticated user found")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
            return false
        }
        
        val userId = getUserId(authentication)
        val tenantId = getTenantId(authentication)
        
        // 检查方法级别的注解
        val methodPermission = handler.getMethodAnnotation(RequiresPermission::class.java)
        val methodRole = handler.getMethodAnnotation(RequiresRole::class.java)
        
        // 检查类级别的注解
        val classPermission = handler.beanType.getAnnotation(RequiresPermission::class.java)
        val classRole = handler.beanType.getAnnotation(RequiresRole::class.java)
        
        // 权限检查（方法级别优先）
        val permissionAnnotation = methodPermission ?: classPermission
        if (permissionAnnotation != null) {
            val domain = if (permissionAnnotation.checkTenant) tenantId else null
            val hasPermission = authorizationService.enforce(
                userId,
                permissionAnnotation.resource,
                permissionAnnotation.action,
                domain
            )
            
            if (!hasPermission) {
                logger.warn("Permission denied: user={}, resource={}, action={}, domain={}", 
                    userId, permissionAnnotation.resource, permissionAnnotation.action, domain)
                response.sendError(HttpServletResponse.SC_FORBIDDEN, permissionAnnotation.message)
                return false
            }
        }
        
        // 角色检查（方法级别优先）
        val roleAnnotation = methodRole ?: classRole
        if (roleAnnotation != null) {
            val domain = if (roleAnnotation.checkTenant) tenantId else null
            val userRoles = authorizationService.getRolesForUser(userId.toInt(), domain)
            
            val hasRole = if (roleAnnotation.requireAll) {
                roleAnnotation.roles.all { it in userRoles }
            } else {
                roleAnnotation.roles.any { it in userRoles }
            }
            
            if (!hasRole) {
                logger.warn("Role check failed: user={}, required={}, actual={}, domain={}", 
                    userId, roleAnnotation.roles.joinToString(), userRoles.joinToString(), domain)
                response.sendError(HttpServletResponse.SC_FORBIDDEN, roleAnnotation.message)
                return false
            }
        }
        
        return true
    }
    
    /**
     * 从认证对象中获取用户ID
     */
    private fun getUserId(authentication: org.springframework.security.core.Authentication): String {
        // 尝试从 Principal 中获取用户ID
        val principal = authentication.principal
        
        // 如果是 UserDetails，尝试获取 username（假设 username 就是用户ID）
        if (principal is org.springframework.security.core.userdetails.UserDetails) {
            return principal.username
        }
        
        // 如果 principal 是字符串，直接返回
        if (principal is String) {
            return principal
        }
        
        // 尝试通过反射获取 id 字段
        try {
            val idField = principal::class.java.getDeclaredField("id")
            idField.isAccessible = true
            return idField.get(principal).toString()
        } catch (e: Exception) {
            logger.warn("Cannot extract user id from principal: {}", principal::class.java.name)
        }
        
        return authentication.name
    }
    
    /**
     * 从认证对象中获取租户ID
     */
    private fun getTenantId(authentication: org.springframework.security.core.Authentication): String? {
        val principal = authentication.principal
        
        // 尝试通过反射获取 tenantId 字段
        try {
            val tenantIdField = principal::class.java.getDeclaredField("tenantId")
            tenantIdField.isAccessible = true
            val tenantId = tenantIdField.get(principal)
            return tenantId?.toString()
        } catch (e: Exception) {
            // 没有 tenantId 字段，返回 null
        }
        
        return null
    }
}

