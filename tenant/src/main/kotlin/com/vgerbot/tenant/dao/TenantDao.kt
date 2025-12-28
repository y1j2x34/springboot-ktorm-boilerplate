package com.vgerbot.tenant.dao

import com.vgerbot.tenant.model.Tenant
import com.vgerbot.tenant.model.Tenants
import com.vgerbot.tenant.model.tenants
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * 租户数据访问对象
 */
@Repository
class TenantDao {
    
    @Autowired
    private lateinit var database: Database
    
    /**
     * 根据 ID 查询租户
     */
    fun findById(id: Int): Tenant? {
        return database.tenants.find { it.id eq id }
    }
    
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
    
    /**
     * 查询所有租户（用于邮箱域名匹配）
     */
    fun findAll(): List<Tenant> {
        return database.tenants.toList()
    }
}

