package com.vgerbot.authorization.entity

import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

/**
 * 用户权限关联实体
 * 用于支持 ACL（Access Control List）模式，允许直接为用户分配权限
 */
interface UserPermission : SimpleAuditableEntity<UserPermission> {
    companion object : Entity.Factory<UserPermission>()
    
    val id: Int
    var userId: Int
    var permissionId: Int
    var tenantId: Int?  // 可选，支持多租户 ACL
}

/**
 * 用户权限关联表结构
 */
object UserPermissions : SimpleAuditableTable<UserPermission>("user_permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val permissionId = int("permission_id").bindTo { it.permissionId }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
}

val Database.userPermissions get() = this.sequenceOf(UserPermissions)

