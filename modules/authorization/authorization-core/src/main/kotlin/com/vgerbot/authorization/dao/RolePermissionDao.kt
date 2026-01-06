package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.RolePermission
import com.vgerbot.authorization.entity.RolePermissions
import com.vgerbot.common.dao.BaseDao

/**
 * 角色权限关联 DAO 接口
 * 
 * 作为纯关联表的 DAO，提供基本的 CRUD 操作和业务查询方法
 */
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
     * 删除角色的特定权限（物理删除）
     */
    fun deleteByRoleIdAndPermissionId(roleId: Int, permissionId: Int): Int
}

