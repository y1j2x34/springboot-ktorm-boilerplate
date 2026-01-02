package com.vgerbot.authorization.dao

/**
 * Casbin 策略数据访问对象
 * 用于从数据库加载 Casbin 策略数据
 */
interface CasbinPolicyDao {
    /**
     * 从 role_permission 表加载策略（RBAC 模式）
     * 返回格式：roleCode, resource, action, tenantId?
     */
    data class RolePermissionPolicy(
        val roleCode: String,
        val resource: String,
        val action: String,
        val tenantId: Int?
    )
    
    /**
     * 从 user_permission 表加载策略（ACL 模式）
     * 返回格式：userId, resource, action, tenantId?
     */
    data class UserPermissionPolicy(
        val userId: Int,
        val resource: String,
        val action: String,
        val tenantId: Int?
    )
    
    /**
     * 从 user_role 表加载分组规则
     * 返回格式：userId, roleCode, tenantId?
     */
    data class UserRoleGrouping(
        val userId: Int,
        val roleCode: String,
        val tenantId: Int?
    )
    
    /**
     * 从 user_tenant 表加载租户分组规则
     * 返回格式：userId, tenantId, tenantCode
     */
    data class UserTenantGrouping(
        val userId: Int,
        val tenantId: Int,
        val tenantCode: String
    )
    
    /**
     * 加载所有角色权限策略
     */
    fun loadRolePermissionPolicies(): List<RolePermissionPolicy>
    
    /**
     * 加载所有用户权限策略（ACL）
     */
    fun loadUserPermissionPolicies(): List<UserPermissionPolicy>
    
    /**
     * 加载所有用户角色分组规则
     */
    fun loadUserRoleGroupings(): List<UserRoleGrouping>
    
    /**
     * 加载所有用户租户分组规则
     */
    fun loadUserTenantGroupings(): List<UserTenantGrouping>
}

