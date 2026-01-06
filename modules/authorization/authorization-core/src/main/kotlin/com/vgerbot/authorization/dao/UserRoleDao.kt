package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.UserRole
import com.vgerbot.authorization.entity.UserRoles
import com.vgerbot.common.dao.BaseDao

/**
 * 用户角色关联 DAO 接口
 * 
 * 作为纯关联表的 DAO，提供基本的 CRUD 操作和业务查询方法
 */
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
     * 删除用户的特定角色（物理删除）
     */
    fun deleteByUserIdAndRoleId(userId: Int, roleId: Int): Int
}

