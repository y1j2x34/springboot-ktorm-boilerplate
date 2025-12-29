package com.vgerbot.tenant.com.vgerbot.tenant.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

/**
 * 用户租户关联实体
 */
interface UserTenant : Entity<UserTenant> {
    companion object : Entity.Factory<UserTenant>()

    val id: Int
    var userId: Int
    var tenantId: Int
    var createdAt: Instant
}

/**
 * 用户租户关联表结构
 */
object UserTenants : Table<UserTenant>("user_tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
}

val Database.userTenants get() = this.sequenceOf(UserTenants)

