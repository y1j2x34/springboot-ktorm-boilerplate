package com.vgerbot.com.vgerbot.tenant.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.tenant.com.vgerbot.tenant.dao.UserTenantDao
import com.vgerbot.tenant.com.vgerbot.tenant.model.UserTenant
import com.vgerbot.tenant.com.vgerbot.tenant.model.UserTenants
import com.vgerbot.tenant.com.vgerbot.tenant.model.userTenants
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.springframework.stereotype.Repository

/**
 * 用户租户关联数据访问对象
 */
@Repository
class UserTenantDaoImpl: AbstractBaseDao<UserTenant, UserTenants>(UserTenants), UserTenantDao {

    /**
     * 根据用户 ID 查询所有关联的租户
     */
    fun findByUserId(userId: Int): List<UserTenant> = findList { it.id eq userId }
    
    /**
     * 根据用户 ID 查询租户（返回第一个关联的租户）
     */
    fun findDefaultByUserId(userId: Int): UserTenant? = findOne { it.userId eq userId }
    
    /**
     * 查询用户在指定租户下的关联记录
     */
    fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant? = findOne {
        (it.userId eq userId) and (it.tenantId eq tenantId)
    }
    
    /**
     * 创建用户租户关联
     */
    fun create(userId: Int, tenantId: Int): Int {
        val userTenant = UserTenant {
            this.userId = userId
            this.tenantId = tenantId
        }
        return database.userTenants.add(userTenant)
    }
}

