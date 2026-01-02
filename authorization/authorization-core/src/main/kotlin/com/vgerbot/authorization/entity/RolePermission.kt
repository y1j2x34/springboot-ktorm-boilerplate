package com.vgerbot.authorization.entity

import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface RolePermission : SimpleAuditableEntity<RolePermission> {
    companion object : Entity.Factory<RolePermission>()
    
    val id: Int
    var roleId: Int
    var permissionId: Int
}

object RolePermissions : SimpleAuditableTable<RolePermission>("role_permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val roleId = int("role_id").bindTo { it.roleId }
    val permissionId = int("permission_id").bindTo { it.permissionId }
}

val Database.rolePermissions get() = this.sequenceOf(RolePermissions)

