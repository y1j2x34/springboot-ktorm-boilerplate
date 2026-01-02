package com.vgerbot.authorization.api

/**
 * 授权服务接口
 * 提供统一的权限检查和管理能力
 */
interface AuthorizationService {
    
    /**
     * 检查用户是否有权限执行指定操作
     * 
     * @param subject 主体（通常是用户ID）
     * @param resource 资源
     * @param action 操作
     * @param domain 域/租户（可选，用于多租户场景）
     * @return 是否有权限
     */
    fun enforce(subject: String, resource: String, action: String, domain: String? = null): Boolean
    
    /**
     * 批量检查权限
     * 
     * @param requests 权限请求列表
     * @return 每个请求的权限检查结果
     */
    fun batchEnforce(requests: List<PermissionRequest>): List<Boolean>
    
    /**
     * 添加策略
     * 
     * @param subject 主体
     * @param resource 资源
     * @param action 操作
     * @param domain 域（可选）
     * @return 是否添加成功
     */
    fun addPolicy(subject: String, resource: String, action: String, domain: String? = null): Boolean
    
    /**
     * 移除策略
     * 
     * @param subject 主体
     * @param resource 资源
     * @param action 操作
     * @param domain 域（可选）
     * @return 是否移除成功
     */
    fun removePolicy(subject: String, resource: String, action: String, domain: String? = null): Boolean
    
    /**
     * 获取主体的所有权限
     * 
     * @param subject 主体
     * @param domain 域（可选）
     * @return 权限列表
     */
    fun getPermissionsForSubject(subject: String, domain: String? = null): List<Permission>
    
    /**
     * 获取资源的所有权限
     * 
     * @param resource 资源
     * @param domain 域（可选）
     * @return 权限列表
     */
    fun getPermissionsForResource(resource: String, domain: String? = null): List<Permission>
    
    /**
     * 添加角色继承关系
     * 
     * @param role 子角色
     * @param parentRole 父角色
     * @param domain 域（可选）
     * @return 是否添加成功
     */
    fun addRoleInheritance(role: String, parentRole: String, domain: String? = null): Boolean
    
    /**
     * 移除角色继承关系
     * 
     * @param role 子角色
     * @param parentRole 父角色
     * @param domain 域（可选）
     * @return 是否移除成功
     */
    fun removeRoleInheritance(role: String, parentRole: String, domain: String? = null): Boolean
    
    /**
     * 获取用户的所有角色
     * 
     * @param userId 用户ID
     * @param domain 域（可选）
     * @return 角色列表
     */
    fun getRolesForUser(userId: Int, domain: String? = null): List<String>
    
    /**
     * 获取拥有指定角色的所有用户
     * 
     * @param role 角色
     * @param domain 域（可选）
     * @return 用户ID列表
     */
    fun getUsersForRole(role: String, domain: String? = null): List<Int>
    
    /**
     * 为用户添加角色
     * 
     * @param userId 用户ID
     * @param role 角色
     * @param domain 域（可选）
     * @return 是否添加成功
     */
    fun addRoleForUser(userId: Int, role: String, domain: String? = null): Boolean
    
    /**
     * 移除用户的角色
     * 
     * @param userId 用户ID
     * @param role 角色
     * @param domain 域（可选）
     * @return 是否移除成功
     */
    fun removeRoleForUser(userId: Int, role: String, domain: String? = null): Boolean
    
    /**
     * 删除用户的所有角色
     * 
     * @param userId 用户ID
     * @param domain 域（可选）
     * @return 是否删除成功
     */
    fun deleteRolesForUser(userId: Int, domain: String? = null): Boolean
    
    /**
     * 删除角色
     * 
     * @param role 角色
     * @param domain 域（可选）
     * @return 是否删除成功
     */
    fun deleteRole(role: String, domain: String? = null): Boolean
    
    /**
     * 删除权限
     * 
     * @param resource 资源
     * @param action 操作
     * @param domain 域（可选）
     * @return 是否删除成功
     */
    fun deletePermission(resource: String, action: String, domain: String? = null): Boolean
    
    /**
     * 重新加载策略
     */
    fun reloadPolicy()
}

/**
 * 权限请求
 */
data class PermissionRequest(
    val subject: String,
    val resource: String,
    val action: String,
    val domain: String? = null
)

/**
 * 权限
 */
data class Permission(
    val subject: String,
    val resource: String,
    val action: String,
    val domain: String? = null
)

