package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.UserRole
import com.vgerbot.authorization.entity.UserRoles
import com.vgerbot.common.dao.SimpleAuditableDaoImpl
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

@Repository
class UserRoleDaoImpl : SimpleAuditableDaoImpl<UserRole, UserRoles>(UserRoles), UserRoleDao {
    
    /**
     * 根据用户ID获取所有角色ID列表
     */
    override fun getRoleIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserRoles)
            .select(UserRoles.roleId)
            .where { (UserRoles.userId eq userId) and (UserRoles.isDeleted eq false) }
            .map { it[UserRoles.roleId]!! }
    }
    
    /**
     * 检查用户是否已分配某个角色
     */
    override fun existsByUserIdAndRoleId(userId: Int, roleId: Int): Boolean {
        return findOneActive { 
            (it.userId eq userId) and (it.roleId eq roleId) 
        } != null
    }
    
    /**
     * 删除用户的特定角色（逻辑删除）
     */
    override fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int {
        return softDeleteIf {
            (it.userId eq userId) and (it.roleId eq roleId) 
        }
    }
}

