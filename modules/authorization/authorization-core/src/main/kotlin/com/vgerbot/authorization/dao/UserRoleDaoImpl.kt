package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.UserRole
import com.vgerbot.authorization.entity.UserRoles
import com.vgerbot.common.dao.AbstractBaseDao
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

/**
 * 用户角色关联 DAO 实现
 * 
 * 作为纯关联表，使用物理删除，不支持逻辑删除
 */
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
     * 删除用户的特定角色（物理删除）
     */
    override fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int {
        return deleteIf {
            (it.userId eq userId) and (it.roleId eq roleId) 
        }
    }
}

