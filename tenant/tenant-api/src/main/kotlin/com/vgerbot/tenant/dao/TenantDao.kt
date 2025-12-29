package com.vgerbot.tenant.com.vgerbot.tenant.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.tenant.com.vgerbot.tenant.model.Tenant
import com.vgerbot.tenant.com.vgerbot.tenant.model.Tenants

interface TenantDao: BaseDao<Tenant, Tenants> {
    fun findById(id: Int): Tenant?
}
