package com.vgerbot.com.vgerbot.tenant.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.tenant.com.vgerbot.tenant.dao.TenantDao
import com.vgerbot.tenant.com.vgerbot.tenant.model.Tenant
import com.vgerbot.tenant.com.vgerbot.tenant.model.Tenants
import com.vgerbot.tenant.com.vgerbot.tenant.model.tenants
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.springframework.stereotype.Repository

/**
 * 租户数据访问对象
 */
@Repository
class TenantDaoImpl: AbstractBaseDao<Tenant, Tenants>(Tenants), TenantDao {

    
    /**
     * 根据 ID 查询租户
     */
    override fun findById(id: Int): Tenant? =
        database.tenants.find { it.id eq id }

    
    /**
     * 根据租户代码查询
     */
    fun findByCode(code: String): Tenant? {
        return database.tenants.find { it.code eq code }
    }
    
    /**
     * 查询所有激活的租户
     */
    fun findAllActive(): List<Tenant> {
        return database.tenants.filter { it.status eq 1 }.toList()
    }

}

