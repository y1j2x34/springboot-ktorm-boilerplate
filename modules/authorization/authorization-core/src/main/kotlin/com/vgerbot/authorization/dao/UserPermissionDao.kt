package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.UserPermission
import com.vgerbot.authorization.entity.UserPermissions
import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.common.dao.BaseDao
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

/**
 * 用户权限关联 DAO 接口
 * 
 * 作为纯关联表的 DAO，提供基本的 CRUD 操作和业务查询方法
 */
interface UserPermissionDao : BaseDao<UserPermission, UserPermissions> {
    /**
     * 根据用户ID获取所有权限ID列表
     */
    fun getPermissionIdsByUserId(userId: Int): List<Int>
    
    /**
     * 根据用户ID和租户ID获取所有权限ID列表
     */
    fun getPermissionIdsByUserIdAndTenantId(userId: Int, tenantId: Int): List<Int>
    
    /**
     * 检查用户是否已直接分配某个权限
     */
    fun existsByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int? = null): Boolean
    
    /**
     * 删除用户的特定权限（物理删除）
     */
    fun deleteByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int? = null): Int
    
    /**
     * 根据用户ID和权限ID查找
     */
    fun findByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int? = null): UserPermission?
}

/**
 * 用户权限关联 DAO 实现
 * 
 * 作为纯关联表，使用物理删除，不支持逻辑删除
 */
@Repository
class UserPermissionDaoImpl : AbstractBaseDao<UserPermission, UserPermissions>(UserPermissions), UserPermissionDao {
    
    override fun getPermissionIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserPermissions)
            .select(UserPermissions.permissionId)
            .where { UserPermissions.userId eq userId }
            .map { it[UserPermissions.permissionId]!! }
    }
    
    override fun getPermissionIdsByUserIdAndTenantId(userId: Int, tenantId: Int): List<Int> {
        return database
            .from(UserPermissions)
            .select(UserPermissions.permissionId)
            .where { 
                (UserPermissions.userId eq userId) and (UserPermissions.tenantId eq tenantId)
            }
            .map { it[UserPermissions.permissionId]!! }
    }
    
    override fun existsByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int?): Boolean {
        return findByUserIdAndPermissionId(userId, permissionId, tenantId) != null
    }
    
    override fun deleteByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int?): Int {
        return if (tenantId != null) {
            deleteIf {
                (it.userId eq userId) and (it.permissionId eq permissionId) and (it.tenantId eq tenantId)
            }
        } else {
            deleteIf {
                (it.userId eq userId) and (it.permissionId eq permissionId)
            }
        }
    }
    
    override fun findByUserIdAndPermissionId(userId: Int, permissionId: Int, tenantId: Int?): UserPermission? {
        return if (tenantId != null) {
            findOne { 
                (it.userId eq userId) and (it.permissionId eq permissionId) and (it.tenantId eq tenantId)
            }
        } else {
            findOne { 
                (it.userId eq userId) and (it.permissionId eq permissionId)
            }
        }
    }
}

