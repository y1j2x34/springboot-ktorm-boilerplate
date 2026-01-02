package com.vgerbot.authorization.service

import com.vgerbot.authorization.dto.PermissionDto
import com.vgerbot.authorization.dto.RoleDto

/**
 * RBAC 数据管理服务接口
 * 用于管理用户角色关联、角色权限关联和用户直接权限（ACL）
 */
interface RbacDataService {
    // 用户角色管理
    fun assignRoleToUser(userId: Int, roleId: Int): Boolean
    fun removeRoleFromUser(userId: Int, roleId: Int): Boolean
    fun getUserRoles(userId: Int): List<RoleDto>
    fun getUserPermissions(userId: Int): List<PermissionDto>
    
    // 角色权限管理
    fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean
    fun removePermissionFromRole(roleId: Int, permissionId: Int): Boolean
    fun getRolePermissions(roleId: Int): List<PermissionDto>
    
    // ACL: 用户直接权限管理
    fun assignPermissionToUser(userId: Int, permissionId: Int, tenantId: Int? = null): Boolean
    fun removePermissionFromUser(userId: Int, permissionId: Int, tenantId: Int? = null): Boolean
    fun getUserDirectPermissions(userId: Int, tenantId: Int? = null): List<PermissionDto>
    
    // 获取用户所有权限（包括通过角色获得的权限和直接分配的权限）
    fun getAllUserPermissions(userId: Int, tenantId: Int? = null): List<PermissionDto>
    
    // 权限检查（内部使用，用于与数据库交互）
    fun hasPermissionInDb(userId: Int, permissionCode: String): Boolean
    fun hasRoleInDb(userId: Int, roleCode: String): Boolean
    fun hasDirectPermissionInDb(userId: Int, permissionCode: String, tenantId: Int? = null): Boolean
}

