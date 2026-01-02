package com.vgerbot.tenant.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.common.dao.SimpleAuditableDaoImpl
import com.vgerbot.tenant.entity.UserTenant
import com.vgerbot.tenant.entity.UserTenants
import org.ktorm.dsl.*
import org.springframework.stereotype.Repository

interface UserTenantDao : BaseDao<UserTenant, UserTenants> {
    fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant?
    fun getTenantIdsByUserId(userId: Int): List<Int>
}

@Repository
class UserTenantDaoImpl : SimpleAuditableDaoImpl<UserTenant, UserTenants>(UserTenants), UserTenantDao {
    
    override fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant? {
        return findOneActive { 
            (it.userId eq userId) and (it.tenantId eq tenantId) 
        }
    }
    
    override fun getTenantIdsByUserId(userId: Int): List<Int> {
        return database
            .from(UserTenants)
            .select(UserTenants.tenantId)
            .where { (UserTenants.userId eq userId) and (UserTenants.isDeleted eq false) }
            .map { it[UserTenants.tenantId]!! }
    }
}
