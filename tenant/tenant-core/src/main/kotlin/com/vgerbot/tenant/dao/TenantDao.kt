package com.vgerbot.tenant.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.common.dao.BaseDao
import com.vgerbot.tenant.entity.Tenant
import com.vgerbot.tenant.entity.Tenants
import org.ktorm.dsl.eq
import org.springframework.stereotype.Repository

interface TenantDao: BaseDao<Tenant, Tenants> {
    fun findById(id: Int): Tenant?
}

@Repository
class TenantDaoImpl: AbstractBaseDao<Tenant, Tenants>(Tenants), TenantDao {
    override fun findById(id: Int): Tenant? = findOne { it.id eq id }
}