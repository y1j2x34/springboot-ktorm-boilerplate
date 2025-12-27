package com.vgerbot.rbac.interceptor

import com.vgerbot.common.annotation.PublicAccess
import com.vgerbot.common.annotation.RequiresPermission
import com.vgerbot.common.annotation.RequiresPermissions
import com.vgerbot.common.annotation.RequiresRole
import com.vgerbot.common.security.UserIdentityResolver
import com.vgerbot.rbac.service.RbacService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * RBAC 权限拦截器
 * 
 * 通过 UserIdentityResolver 接口解析用户身份，然后检查权限
 * 支持多种认证方式（JWT、Session 等），只要提供相应的 UserIdentityResolver 实现即可
 */
@Component
class JwtRbacInterceptor : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(JwtRbacInterceptor::class.java)
    
    @Autowired
    lateinit var rbacService: RbacService
    
    @Autowired(required = false)
    var userIdentityResolver: UserIdentityResolver? = null
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }
        
        // 检查是否标记为公开访问
        if (isPublicAccess(handler)) {
            logger.debug("公开访问: ${request.requestURI}")
            return true
        }
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            logger.warn("未登录: ${request.requestURI}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录")
            return false
        }
        // 从认证系统中获取用户ID
        val userId = getUserId(authentication.principal)
        if (userId == null) {
            logger.warn("未登录或认证无效: ${request.requestURI}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录")
            return false
        }
        
        // 检查角色权限
        val requiresRole = getRequiresRoleAnnotation(handler)
        if (requiresRole != null && !checkRole(userId, requiresRole)) {
            logger.warn("用户 $userId 无权访问: ${request.requestURI}, 缺少角色")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足")
            return false
        }
        
        // 检查操作权限
        val permissionCodes = getRequiredPermissions(handler)
        if (permissionCodes.isNotEmpty()) {
            val requireAll = getRequireAllFlag(handler)
            if (!checkPermission(userId, permissionCodes, requireAll)) {
                logger.warn("用户 $userId 无权访问: ${request.requestURI}, 缺少权限: $permissionCodes")
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足")
                return false
            }
        }
        
        logger.debug("用户 $userId 有权访问: ${request.requestURI}")
        return true
    }
    
    /**
     * 从请求中获取用户ID
     */
    private fun getUserId(principal: Any?): Int? {
        return when (principal) {
            is UserDetails -> {
                // 从 UserDetails 的 username 获取（假设 username 是用户ID）
                principal.username.toIntOrNull()
            }
            is String -> {
                // 如果 principal 直接是字符串
                principal.toIntOrNull()
            }
            else -> {
                logger.warn("未知的 Principal 类型: ${principal?.javaClass}")
                null
            }
        }
    }
    
    private fun isPublicAccess(handler: HandlerMethod): Boolean {
        if (handler.getMethodAnnotation(PublicAccess::class.java) != null) {
            return true
        }
        if (handler.beanType.getAnnotation(PublicAccess::class.java) != null) {
            return true
        }
        return false
    }
    
    private fun getRequiredPermissions(handler: HandlerMethod): List<String> {
        val permissions = mutableListOf<String>()
        
        handler.getMethodAnnotation(RequiresPermission::class.java)?.let { annotation ->
            permissions.add(buildPermissionCode(annotation))
        }
        
        handler.getMethodAnnotation(RequiresPermissions::class.java)?.let { annotation ->
            annotation.value.forEach { permissions.add(buildPermissionCode(it)) }
        }
        
        handler.beanType.getAnnotation(RequiresPermission::class.java)?.let { annotation ->
            permissions.add(buildPermissionCode(annotation))
        }
        
        handler.beanType.getAnnotation(RequiresPermissions::class.java)?.let { annotation ->
            annotation.value.forEach { permissions.add(buildPermissionCode(it)) }
        }
        
        return permissions
    }
    
    private fun buildPermissionCode(annotation: RequiresPermission): String {
        return if (annotation.value.isNotEmpty()) {
            annotation.value
        } else {
            "${annotation.resource}:${annotation.action}"
        }
    }
    
    private fun getRequireAllFlag(handler: HandlerMethod): Boolean {
        handler.getMethodAnnotation(RequiresPermissions::class.java)?.let {
            return it.requireAll
        }
        handler.getMethodAnnotation(RequiresPermission::class.java)?.let {
            return it.requireAll
        }
        handler.beanType.getAnnotation(RequiresPermissions::class.java)?.let {
            return it.requireAll
        }
        return false
    }
    
    private fun getRequiresRoleAnnotation(handler: HandlerMethod): RequiresRole? {
        handler.getMethodAnnotation(RequiresRole::class.java)?.let {
            return it
        }
        return handler.beanType.getAnnotation(RequiresRole::class.java)
    }
    
    private fun checkPermission(
        userId: Int,
        permissionCodes: List<String>,
        requireAll: Boolean
    ): Boolean {
        return if (requireAll) {
            permissionCodes.all { rbacService.hasPermission(userId, it) }
        } else {
            permissionCodes.any { rbacService.hasPermission(userId, it) }
        }
    }
    
    private fun checkRole(userId: Int, requiresRole: RequiresRole): Boolean {
        val roleCodes = requiresRole.value.toList()
        return if (requiresRole.requireAll) {
            roleCodes.all { rbacService.hasRole(userId, it) }
        } else {
            roleCodes.any { rbacService.hasRole(userId, it) }
        }
    }
}

