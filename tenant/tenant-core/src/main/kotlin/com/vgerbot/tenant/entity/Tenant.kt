package com.vgerbot.tenant.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import com.vgerbot.common.utils.EmailDomainMatcher
import com.vgerbot.tenant.com.vgerbot.tenant.dto.TenantInfo
import com.vgerbot.tenant.com.vgerbot.tenant.dto.TenantStatus
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

/**
 * 租户实体
 */
interface Tenant : AuditableEntity<Tenant> {
    companion object : Entity.Factory<Tenant>()

    val id: Int
    var code: String
    var name: String
    var description: String?
    var emailDomains: String?
    var status: Int
}

/**
 * 租户表结构
 */
object Tenants : AuditableTable<Tenant>("tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val code = varchar("code").bindTo { it.code }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.description }
    val emailDomains = varchar("email_domains").bindTo { it.emailDomains }
    val status = int("status").bindTo { it.status }
}

val Database.tenants get() = this.sequenceOf(Tenants)

fun Tenant.toDto() = TenantInfo(
    this.id,
    this.code,
    this.name,
    this.description ?: "",
    EmailDomainMatcher.expandPattern(this.emailDomains ?: "*"),
    TenantStatus.from(this.status)
)