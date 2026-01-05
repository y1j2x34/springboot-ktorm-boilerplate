package com.vgerbot.tenant.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.common.dao.BaseDao
import com.vgerbot.tenant.entity.UserTenant
import com.vgerbot.tenant.entity.UserTenants
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

/**
 * 用户租户关联 DAO 接口
 * 
 * 作为纯关联表的 DAO，提供基本的 CRUD 操作和业务查询方法
 */
interface UserTenantDao : BaseDao<UserTenant, UserTenants> {
    /**
     * 根据用户ID和租户ID查找关联关系
     */
    fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant?
    
    /**
     * 根据用户ID获取所有租户ID列表
     */
    fun getTenantIdsByUserId(userId: Int): List<Int>
}

/**
 * 用户租户关联 DAO 实现
 * 
 * 作为纯关联表，使用物理删除，不支持逻辑删除
 */
@Repository
class UserTenantDaoImpl : AbstractBaseDao<UserTenant, UserTenants>(UserTenants), UserTenantDao {
    
    override fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant? {
        return findOne { 
            (it.userId eq userId) and (it.tenantId eq tenantId) 
        }
    }
    
    override fun getTenantIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserTenants)
            .select(UserTenants.tenantId)
            .where { UserTenants.userId eq userId }
            .map { it[UserTenants.tenantId]!! }
    }
}
