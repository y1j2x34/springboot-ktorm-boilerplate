package com.vgerbot.rbac.service

import com.vgerbot.rbac.model.Permission
import com.vgerbot.rbac.model.Role

interface RbacService {
    // 用户角色管理
    fun assignRoleToUser(userId: Int, roleId: Int): Boolean
    fun removeRoleFromUser(userId: Int, roleId: Int): Boolean
    fun getUserRoles(userId: Int): List<Role>
    fun getUserPermissions(userId: Int): List<Permission>
    
    // 角色权限管理
    fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean
    fun removePermissionFromRole(roleId: Int, permissionId: Int): Boolean
    fun getRolePermissions(roleId: Int): List<Permission>
    
    // 权限检查
    fun hasPermission(userId: Int, permissionCode: String): Boolean
    fun hasRole(userId: Int, roleCode: String): Boolean
}

