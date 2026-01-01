package com.vgerbot.rbac.service

import com.vgerbot.rbac.dto.PermissionDto
import com.vgerbot.rbac.dto.RoleDto

interface RbacService {
    // 用户角色管理
    fun assignRoleToUser(userId: Int, roleId: Int): Boolean
    fun removeRoleFromUser(userId: Int, roleId: Int): Boolean
    fun getUserRoles(userId: Int): List<RoleDto>
    fun getUserPermissions(userId: Int): List<PermissionDto>
    
    // 角色权限管理
    fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean
    fun removePermissionFromRole(roleId: Int, permissionId: Int): Boolean
    fun getRolePermissions(roleId: Int): List<PermissionDto>
    
    // 权限检查
    fun hasPermission(userId: Int, permissionCode: String): Boolean
    fun hasRole(userId: Int, roleCode: String): Boolean
}

