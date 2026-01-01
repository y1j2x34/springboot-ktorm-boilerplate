package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.rbac.entity.RolePermission
import com.vgerbot.rbac.entity.RolePermissions

interface RolePermissionDao : BaseDao<RolePermission, RolePermissions> {
    /**
     * 根据角色ID列表获取所有权限ID列表
     */
    fun getPermissionIdsByRoleIds(roleIds: List<Int>): List<Int>
    
    /**
     * 根据角色ID获取所有权限ID列表
     */
    fun getPermissionIdsByRoleId(roleId: Int): List<Int>
    
    /**
     * 检查角色是否已分配某个权限
     */
    fun existsByRoleIdAndPermissionId(roleId: Int, permissionId: Int): Boolean
    
    /**
     * 删除角色的特定权限
     */
    fun deleteByRoleIdAndPermissionId(roleId: Int, permissionId: Int): Int
}

