package com.vgerbot.rbac.security

import com.vgerbot.rbac.service.RbacService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

/**
 * RBAC 权限加载器
 * 
 * 用于将 RBAC 模块的权限集成到 Spring Security 中
 */
@Component
class RbacAuthoritiesLoader {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    /**
     * 加载用户的所有权限为 Spring Security 的 GrantedAuthority
     * 
     * @param userId 用户ID
     * @return Spring Security 权限列表
     */
    fun loadAuthorities(userId: Int): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        
        // 加载角色
        val roles = rbacService.getUserRoles(userId)
        roles.forEach { role ->
            // 添加角色，Spring Security 角色需要 ROLE_ 前缀
            authorities.add(SimpleGrantedAuthority(role.code))
        }
        
        // 加载权限
        val permissions = rbacService.getUserPermissions(userId)
        permissions.forEach { permission ->
            // 添加权限
            authorities.add(SimpleGrantedAuthority(permission.code))
        }
        
        return authorities
    }
    
    /**
     * 只加载用户的角色
     */
    fun loadRoles(userId: Int): Collection<GrantedAuthority> {
        return rbacService.getUserRoles(userId).map { role ->
            SimpleGrantedAuthority(role.code)
        }
    }
    
    /**
     * 只加载用户的权限
     */
    fun loadPermissions(userId: Int): Collection<GrantedAuthority> {
        return rbacService.getUserPermissions(userId).map { permission ->
            SimpleGrantedAuthority(permission.code)
        }
    }
}

/**
 * 自定义的权限检查器
 * 
 * 可以在 @PreAuthorize 注解中使用
 * 例如: @PreAuthorize("@rbacChecker.hasPermission(authentication, 'user:delete')")
 */
@Component("rbacChecker")
class RbacPermissionChecker {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    /**
     * 检查当前用户是否拥有指定权限
     * 
     * 使用示例:
     * @PreAuthorize("@rbacChecker.hasPermission(#userId, 'user:delete')")
     */
    fun hasPermission(userId: Int, permissionCode: String): Boolean {
        return rbacService.hasPermission(userId, permissionCode)
    }
    
    /**
     * 检查当前用户是否拥有指定角色
     * 
     * 使用示例:
     * @PreAuthorize("@rbacChecker.hasRole(#userId, 'ROLE_ADMIN')")
     */
    fun hasRole(userId: Int, roleCode: String): Boolean {
        return rbacService.hasRole(userId, roleCode)
    }
    
    /**
     * 检查用户是否拥有任意一个指定的权限
     */
    fun hasAnyPermission(userId: Int, vararg permissionCodes: String): Boolean {
        return permissionCodes.any { rbacService.hasPermission(userId, it) }
    }
    
    /**
     * 检查用户是否拥有所有指定的权限
     */
    fun hasAllPermissions(userId: Int, vararg permissionCodes: String): Boolean {
        return permissionCodes.all { rbacService.hasPermission(userId, it) }
    }
    
    /**
     * 检查用户是否拥有任意一个指定的角色
     */
    fun hasAnyRole(userId: Int, vararg roleCodes: String): Boolean {
        return roleCodes.any { rbacService.hasRole(userId, it) }
    }
    
    /**
     * 检查用户是否拥有所有指定的角色
     */
    fun hasAllRoles(userId: Int, vararg roleCodes: String): Boolean {
        return roleCodes.all { rbacService.hasRole(userId, it) }
    }
}

