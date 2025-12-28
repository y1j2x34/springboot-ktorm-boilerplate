package com.vgerbot.tenant.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

/**
 * 租户实体
 */
interface Tenant : Entity<Tenant> {
    companion object : Entity.Factory<Tenant>()

    val id: Int
    var code: String
    var name: String
    var description: String?
    var emailDomains: String?
    var status: Int
    var createdAt: Instant
    var updatedAt: Instant?
}

/**
 * 租户表结构
 */
object Tenants : Table<Tenant>("tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val code = varchar("code").bindTo { it.code }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.description }
    val emailDomains = varchar("email_domains").bindTo { it.emailDomains }
    val status = int("status").bindTo { it.status }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
}

val Database.tenants get() = this.sequenceOf(Tenants)

