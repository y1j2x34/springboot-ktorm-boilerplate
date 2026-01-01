package com.vgerbot.rbac.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface RolePermission : Entity<RolePermission> {
    companion object : Entity.Factory<RolePermission>()
    
    val id: Int
    var roleId: Int
    var permissionId: Int
    var createdAt: Instant
}

object RolePermissions : Table<RolePermission>("role_permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val roleId = int("role_id").bindTo { it.roleId }
    val permissionId = int("permission_id").bindTo { it.permissionId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
}

val Database.rolePermissions get() = this.sequenceOf(RolePermissions)

