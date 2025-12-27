package com.vgerbot.rbac.interceptor

import com.vgerbot.common.annotation.PublicAccess
import com.vgerbot.common.annotation.RequiresPermission
import com.vgerbot.common.annotation.RequiresPermissions
import com.vgerbot.common.annotation.RequiresRole
import com.vgerbot.rbac.service.RbacService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * RBAC 权限拦截器
 * 
 * 拦截所有标记了权限注解的请求，检查用户是否有相应权限
 */
@Component
class RbacInterceptor : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(RbacInterceptor::class.java)
    
    @Autowired
    lateinit var rbacService: RbacService
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 只处理 Controller 方法
        if (handler !is HandlerMethod) {
            return true
        }
        
        // 检查是否标记为公开访问
        if (isPublicAccess(handler)) {
            logger.debug("公开访问: ${request.requestURI}")
            return true
        }
        
        // 获取当前用户ID（从请求中获取）
        val userId = getCurrentUserId(request)
        if (userId == null) {
            logger.warn("未登录用户尝试访问: ${request.requestURI}")
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
     * 检查是否标记为公开访问
     */
    private fun isPublicAccess(handler: HandlerMethod): Boolean {
        // 检查方法级别
        if (handler.getMethodAnnotation(PublicAccess::class.java) != null) {
            return true
        }
        // 检查类级别
        if (handler.beanType.getAnnotation(PublicAccess::class.java) != null) {
            return true
        }
        return false
    }
    
    /**
     * 获取所需的权限码列表
     */
    private fun getRequiredPermissions(handler: HandlerMethod): List<String> {
        val permissions = mutableListOf<String>()
        
        // 检查方法级别的单个权限注解
        handler.getMethodAnnotation(RequiresPermission::class.java)?.let { annotation ->
            permissions.add(buildPermissionCode(annotation))
        }
        
        // 检查方法级别的多个权限注解
        handler.getMethodAnnotation(RequiresPermissions::class.java)?.let { annotation ->
            annotation.value.forEach { permissions.add(buildPermissionCode(it)) }
        }
        
        // 检查类级别的权限注解
        handler.beanType.getAnnotation(RequiresPermission::class.java)?.let { annotation ->
            permissions.add(buildPermissionCode(annotation))
        }
        
        handler.beanType.getAnnotation(RequiresPermissions::class.java)?.let { annotation ->
            annotation.value.forEach { permissions.add(buildPermissionCode(it)) }
        }
        
        return permissions
    }
    
    /**
     * 构建权限码
     */
    private fun buildPermissionCode(annotation: RequiresPermission): String {
        return if (annotation.value.isNotEmpty()) {
            annotation.value
        } else {
            "${annotation.resource}:${annotation.action}"
        }
    }
    
    /**
     * 获取是否需要所有权限
     */
    private fun getRequireAllFlag(handler: HandlerMethod): Boolean {
        // 先检查方法级别
        handler.getMethodAnnotation(RequiresPermissions::class.java)?.let {
            return it.requireAll
        }
        handler.getMethodAnnotation(RequiresPermission::class.java)?.let {
            return it.requireAll
        }
        
        // 再检查类级别
        handler.beanType.getAnnotation(RequiresPermissions::class.java)?.let {
            return it.requireAll
        }
        
        return false
    }
    
    /**
     * 获取角色注解
     */
    private fun getRequiresRoleAnnotation(handler: HandlerMethod): RequiresRole? {
        // 先检查方法级别
        handler.getMethodAnnotation(RequiresRole::class.java)?.let {
            return it
        }
        // 再检查类级别
        return handler.beanType.getAnnotation(RequiresRole::class.java)
    }
    
    /**
     * 检查用户权限
     */
    private fun checkPermission(
        userId: Int,
        permissionCodes: List<String>,
        requireAll: Boolean
    ): Boolean {
        return if (requireAll) {
            // 需要拥有所有权限
            permissionCodes.all { rbacService.hasPermission(userId, it) }
        } else {
            // 只需要拥有其中一个权限
            permissionCodes.any { rbacService.hasPermission(userId, it) }
        }
    }
    
    /**
     * 检查用户角色
     */
    private fun checkRole(userId: Int, requiresRole: RequiresRole): Boolean {
        val roleCodes = requiresRole.value.toList()
        return if (requiresRole.requireAll) {
            // 需要拥有所有角色
            roleCodes.all { rbacService.hasRole(userId, it) }
        } else {
            // 只需要拥有其中一个角色
            roleCodes.any { rbacService.hasRole(userId, it) }
        }
    }
    
    /**
     * 从请求中获取当前用户ID
     * 
     * 这里提供了多种获取方式，你可以根据实际情况选择：
     * 1. 从请求参数中获取
     * 2. 从请求头中获取
     * 3. 从 JWT Token 中解析
     * 4. 从 Session 中获取
     */
    private fun getCurrentUserId(request: HttpServletRequest): Int? {
        // 方式1: 从请求参数中获取（仅用于测试）
        request.getParameter("userId")?.toIntOrNull()?.let {
            return it
        }
        
        // 方式2: 从请求头中获取
        request.getHeader("X-User-Id")?.toIntOrNull()?.let {
            return it
        }
        
        // 方式3: 从 JWT Token 中解析（需要集成 JWT）
        // val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
        // if (token != null) {
        //     return jwtTokenUtils.getUserIdFromToken(token)
        // }
        
        // 方式4: 从 Session 中获取
        // val session = request.getSession(false)
        // return session?.getAttribute("userId") as? Int
        
        // 方式5: 从 Spring Security Context 中获取
        // val authentication = SecurityContextHolder.getContext().authentication
        // if (authentication != null && authentication.isAuthenticated) {
        //     return (authentication.principal as? UserDetails)?.username?.toIntOrNull()
        // }
        
        return null
    }
}

