package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.entity.UserRole
import com.vgerbot.rbac.entity.UserRoles
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

@Repository
class UserRoleDaoImpl : AbstractBaseDao<UserRole, UserRoles>(UserRoles), UserRoleDao {
    
    /**
     * 根据用户ID获取所有角色ID列表
     */
    override fun getRoleIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserRoles)
            .select(UserRoles.roleId)
            .where { UserRoles.userId eq userId }
            .map { it[UserRoles.roleId]!! }
    }
    
    /**
     * 检查用户是否已分配某个角色
     */
    override fun existsByUserIdAndRoleId(userId: Int, roleId: Int): Boolean {
        return findOne { 
            (it.userId eq userId) and (it.roleId eq roleId) 
        } != null
    }
    
    /**
     * 删除用户的特定角色
     */
    override fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int {
        return deleteIf {
            (it.userId eq userId) and (it.roleId eq roleId) 
        }
    }
}

