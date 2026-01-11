package com.vgerbot.authorization.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar

/**
 * 租户表结构（仅用于查询，不依赖 tenant-core）
 * 这些表对象仅用于 Casbin 策略加载时的 JOIN 查询
 */
interface TenantTableEntity : AuditableEntity<TenantTableEntity> {
    companion object : Entity.Factory<TenantTableEntity>()
    val id: Int
    var code: String
    var name: String
    var description: String?
    var emailDomains: String?
    var status: Int
}

object Tenants : AuditableTable<TenantTableEntity>("tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val code = varchar("code").bindTo { it.code }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.description }
    val emailDomains = varchar("email_domains").bindTo { it.emailDomains }
    val status = int("status").bindTo { it.status }
}

/**
 * 用户租户关联表结构（仅用于查询，不依赖 tenant-core）
 * 作为纯关联表，不支持逻辑删除，只记录创建时间
 */
interface UserTenantTableEntity : Entity<UserTenantTableEntity> {
    companion object : Entity.Factory<UserTenantTableEntity>()
    val id: Int
    var userId: Int
    var tenantId: Int
    var createdAt: java.time.Instant
}

object UserTenants : Table<UserTenantTableEntity>("user_tenant") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
}

