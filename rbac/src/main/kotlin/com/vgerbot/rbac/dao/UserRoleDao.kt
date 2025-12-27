package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.rbac.model.UserRole
import com.vgerbot.rbac.model.UserRoles
import org.ktorm.dsl.*
import org.springframework.stereotype.Component

@Component
class UserRoleDao : BaseDao<UserRole, UserRoles>(UserRoles) {
    
    /**
     * 根据用户ID获取所有角色ID列表
     */
    fun getRoleIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserRoles)
            .select(UserRoles.roleId)
            .where { UserRoles.userId eq userId }
            .map { it[UserRoles.roleId]!! }
    }
    
    /**
     * 检查用户是否已分配某个角色
     */
    fun existsByUserIdAndRoleId(userId: Int, roleId: Int): Boolean {
        return findOne { 
            (it.userId eq userId) and (it.roleId eq roleId) 
        } != null
    }
    
    /**
     * 删除用户的特定角色
     */
    fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int {
        return deleteIf {
            (it.userId eq userId) and (it.roleId eq roleId) 
        }
    }
}

