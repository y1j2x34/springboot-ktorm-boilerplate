package com.vgerbot.rbac.entity

import com.vgerbot.common.entity.StatusAuditableEntity
import com.vgerbot.common.entity.StatusAuditableTable
import com.vgerbot.rbac.dto.RoleDto
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface Role : StatusAuditableEntity<Role> {
    companion object : Entity.Factory<Role>()
    
    val id: Int
    var name: String
    var code: String
    var description: String?
}

object Roles : StatusAuditableTable<Role>("role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val description = varchar("description").bindTo { it.description }
}

val Database.roles get() = this.sequenceOf(Roles)

fun Role.toDto(): RoleDto = RoleDto(
    id = this.id,
    name = this.name,
    code = this.code,
    description = this.description,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt
)

