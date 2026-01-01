package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.rbac.entity.UserRole
import com.vgerbot.rbac.entity.UserRoles

interface UserRoleDao : BaseDao<UserRole, UserRoles> {
    /**
     * 根据用户ID获取所有角色ID列表
     */
    fun getRoleIdsByUserId(userId: Int): List<Int>
    
    /**
     * 检查用户是否已分配某个角色
     */
    fun existsByUserIdAndRoleId(userId: Int, roleId: Int): Boolean
    
    /**
     * 删除用户的特定角色
     */
    fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int
}

