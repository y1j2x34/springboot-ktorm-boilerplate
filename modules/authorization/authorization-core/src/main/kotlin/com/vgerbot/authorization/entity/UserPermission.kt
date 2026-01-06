package com.vgerbot.authorization.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

/**
 * 用户权限关联实体
 * 
 * 用于支持 ACL（Access Control List）模式，允许直接为用户分配权限
 * 作为纯关联表，不支持逻辑删除，只记录创建时间
 */
interface UserPermission : Entity<UserPermission> {
    companion object : Entity.Factory<UserPermission>()
    
    val id: Int
    var userId: Int
    var permissionId: Int
    var tenantId: Int?  // 可选，支持多租户 ACL
    var createdAt: Instant
}

/**
 * 用户权限关联表结构
 */
object UserPermissions : Table<UserPermission>("user_permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val permissionId = int("permission_id").bindTo { it.permissionId }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val isDeleted = boolean("is_deleted")
}

val Database.userPermissions get() = this.sequenceOf(UserPermissions)

