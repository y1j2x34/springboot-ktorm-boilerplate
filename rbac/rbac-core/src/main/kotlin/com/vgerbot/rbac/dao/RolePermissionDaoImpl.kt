package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.model.RolePermission
import com.vgerbot.rbac.model.RolePermissions
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

@Repository
class RolePermissionDaoImpl : AbstractBaseDao<RolePermission, RolePermissions>(RolePermissions), RolePermissionDao {
    
    /**
     * 根据角色ID列表获取所有权限ID列表
     */
    override fun getPermissionIdsByRoleIds(roleIds: List<Int>): List<Int> {
        if (roleIds.isEmpty()) return emptyList()
        
        return database
            .from(RolePermissions)
            .select(RolePermissions.permissionId)
            .where { RolePermissions.roleId inList roleIds }
            .map { it[RolePermissions.permissionId]!! }
    }
    
    /**
     * 根据角色ID获取所有权限ID列表
     */
    override fun getPermissionIdsByRoleId(roleId: Int): List<Int> {
        return database
            .from(RolePermissions)
            .select(RolePermissions.permissionId)
            .where { RolePermissions.roleId eq roleId }
            .map { it[RolePermissions.permissionId]!! }
    }
    
    /**
     * 检查角色是否已分配某个权限
     */
    override fun existsByRoleIdAndPermissionId(roleId: Int, permissionId: Int): Boolean {
        return findOne { 
            (it.roleId eq roleId) and (it.permissionId eq permissionId) 
        } != null
    }
    
    /**
     * 删除角色的特定权限
     */
    override fun deleteByRoleIdAndPermissionId(roleId: Int, permissionId: Int): Int {
        return deleteIf {
            (it.roleId eq roleId) and (it.permissionId eq permissionId) 
        }
    }
}

