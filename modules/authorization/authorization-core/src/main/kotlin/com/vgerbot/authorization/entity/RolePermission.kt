package com.vgerbot.authorization.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

/**
 * 角色权限关联实体
 * 
 * 作为纯关联表，不支持逻辑删除，只记录创建时间
 */
interface RolePermission : Entity<RolePermission> {
    companion object : Entity.Factory<RolePermission>()
    
    val id: Int
    var roleId: Int
    var permissionId: Int
    var createdAt: Instant
}

/**
 * 角色权限关联表结构
 */
object RolePermissions : Table<RolePermission>("role_permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val roleId = int("role_id").bindTo { it.roleId }
    val permissionId = int("permission_id").bindTo { it.permissionId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val isDeleted = boolean("is_deleted")
}

val Database.rolePermissions get() = this.sequenceOf(RolePermissions)

