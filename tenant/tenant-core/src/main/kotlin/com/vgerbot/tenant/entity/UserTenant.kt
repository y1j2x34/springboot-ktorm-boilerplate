package com.vgerbot.tenant.entity

import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

/**
 * 用户租户关联实体
 */
interface UserTenant : SimpleAuditableEntity<UserTenant> {
    companion object : Entity.Factory<UserTenant>()

    val id: Int
    var userId: Int
    var tenantId: Int
}

/**
 * 用户租户关联表结构
 */
object UserTenants : SimpleAuditableTable<UserTenant>("user_tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
}

val Database.userTenants get() = this.sequenceOf(UserTenants)

