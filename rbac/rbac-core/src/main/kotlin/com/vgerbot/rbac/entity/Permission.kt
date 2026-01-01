package com.vgerbot.rbac.entity

import com.vgerbot.common.entity.StatusAuditableEntity
import com.vgerbot.common.entity.StatusAuditableTable
import com.vgerbot.rbac.dto.PermissionDto
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface Permission : StatusAuditableEntity<Permission> {
    companion object : Entity.Factory<Permission>()

    val id: Int
    var name: String
    var code: String
    var resource: String
    var action: String
    var description: String?
}

object Permissions : StatusAuditableTable<Permission>("permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val resource = varchar("resource").bindTo { it.resource }
    val action = varchar("action").bindTo { it.action }
    val description = varchar("description").bindTo { it.description }
}

val Database.permissions get() = this.sequenceOf(Permissions)

fun Permission.toDto(): PermissionDto = PermissionDto(
    id = this.id,
    name = this.name,
    code = this.code,
    resource = this.resource,
    action = this.action,
    description = this.description,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt
)