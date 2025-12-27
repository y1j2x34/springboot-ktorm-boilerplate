package com.vgerbot.common.security

import javax.servlet.http.HttpServletRequest

/**
 * 用户身份解析器接口
 * 
 * 用于从请求中解析出用户标识（如用户ID、用户名等）
 * 这个接口提供了一个抽象层，使得 RBAC 模块不需要直接依赖具体的认证实现（如 JWT、Session 等）
 */
interface UserIdentityResolver {
    /**
     * 从请求中解析用户ID
     * 
     * @param request HTTP请求对象
     * @return 用户ID，如果无法解析则返回null
     */
    fun resolveUserId(request: HttpServletRequest): Int?
    
    /**
     * 从请求中解析用户名
     * 
     * @param request HTTP请求对象
     * @return 用户名，如果无法解析则返回null
     */
    fun resolveUsername(request: HttpServletRequest): String?
}

