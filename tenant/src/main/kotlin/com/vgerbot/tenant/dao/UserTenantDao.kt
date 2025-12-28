package com.vgerbot.tenant.dao

import com.vgerbot.tenant.model.UserTenant
import com.vgerbot.tenant.model.UserTenants
import com.vgerbot.tenant.model.userTenants
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * 用户租户关联数据访问对象
 */
@Repository
class UserTenantDao {
    
    @Autowired
    private lateinit var database: Database
    
    /**
     * 根据用户 ID 查询所有关联的租户
     */
    fun findByUserId(userId: Int): List<UserTenant> {
        return database.userTenants.filter { it.userId eq userId }.toList()
    }
    
    /**
     * 根据用户 ID 查询租户（返回第一个关联的租户）
     */
    fun findDefaultByUserId(userId: Int): UserTenant? {
        return database.userTenants.find { it.userId eq userId }
    }
    
    /**
     * 查询用户在指定租户下的关联记录
     */
    fun findByUserIdAndTenantId(userId: Int, tenantId: Int): UserTenant? {
        return database.userTenants.find {
            (it.userId eq userId) and (it.tenantId eq tenantId)
        }
    }
}

