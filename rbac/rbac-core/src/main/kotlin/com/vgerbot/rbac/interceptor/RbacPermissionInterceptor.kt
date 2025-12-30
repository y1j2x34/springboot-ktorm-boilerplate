package com.vgerbot.rbac.interceptor

import com.vgerbot.common.annotation.PublicAccess
import com.vgerbot.common.annotation.RequirePermission
import com.vgerbot.common.annotation.RequireRole
import com.vgerbot.rbac.service.RbacService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * RBAC 权限拦截器
 * 
 * 拦截所有请求，检查方法上的权限注解并验证用户权限
 */
@Component
class RbacPermissionInterceptor : HandlerInterceptor {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 如果不是 HandlerMethod，直接放行
        if (handler !is HandlerMethod) {
            return true
        }
        
        val method = handler.method
        val clazz = handler.beanType
        
        // 检查方法或类上是否有 @PublicAccess 注解
        if (method.isAnnotationPresent(PublicAccess::class.java) ||
            clazz.isAnnotationPresent(PublicAccess::class.java)) {
            return true
        }
        
        // 获取当前用户ID（从请求参数、Header 或 Session 中获取）
        val userId = getCurrentUserId(request)
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户未登录")
            return false
        }
        
        // 检查 @RequirePermission 注解
        val requirePermission = method.getAnnotation(RequirePermission::class.java)
            ?: clazz.getAnnotation(RequirePermission::class.java)
        
        if (requirePermission != null) {
            val permissionCode = if (requirePermission.code.isNotEmpty()) {
                requirePermission.code
            } else {
                "${requirePermission.resource}:${requirePermission.action}"
            }
            
            if (!rbacService.hasPermission(userId, permissionCode)) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "权限不足：需要 $permissionCode 权限"
                )
                return false
            }
        }
        
        // 检查 @RequireRole 注解
        val requireRole = method.getAnnotation(RequireRole::class.java)
            ?: clazz.getAnnotation(RequireRole::class.java)
        
        if (requireRole != null) {
            val hasRequiredRole = if (requireRole.requireAll) {
                // 需要拥有所有角色
                requireRole.codes.all { roleCode ->
                    rbacService.hasRole(userId, roleCode)
                }
            } else {
                // 只需拥有任意一个角色
                requireRole.codes.any { roleCode ->
                    rbacService.hasRole(userId, roleCode)
                }
            }
            
            if (!hasRequiredRole) {
                val roleMessage = if (requireRole.requireAll) {
                    "需要拥有所有角色：${requireRole.codes.joinToString(", ")}"
                } else {
                    "需要拥有以下角色之一：${requireRole.codes.joinToString(", ")}"
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足：$roleMessage")
                return false
            }
        }
        
        return true
    }
    
    /**
     * 获取当前用户ID
     * 
     * 优先级:
     * 1. Header 中的 X-User-Id
     * 2. 请求参数中的 userId
     * 3. Session 中的 userId
     * 
     * 实际项目中应该从 JWT token 或 Session 中获取
     */
    private fun getCurrentUserId(request: HttpServletRequest): Int? {
        // 从 Header 获取
        request.getHeader("X-User-Id")?.toIntOrNull()?.let {
            return it
        }
        
        // 从请求参数获取
        request.getParameter("userId")?.toIntOrNull()?.let {
            return it
        }
        
        // 从 Session 获取
        request.session.getAttribute("userId")?.let {
            return it as? Int
        }
        
        return null
    }
}

