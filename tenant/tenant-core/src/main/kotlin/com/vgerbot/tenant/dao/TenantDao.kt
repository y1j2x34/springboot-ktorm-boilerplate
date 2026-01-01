package com.vgerbot.tenant.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.tenant.entity.Tenant
import com.vgerbot.tenant.entity.Tenants
import org.ktorm.dsl.eq
import org.springframework.stereotype.Repository

interface TenantDao: SoftDeleteDao<Tenant, Tenants> {
    fun findById(id: Int): Tenant?
}

@Repository
class TenantDaoImpl: AuditableDaoImpl<Tenant, Tenants>(Tenants), TenantDao {
    override fun findById(id: Int): Tenant? = findOneActive { it.id eq id }
}
