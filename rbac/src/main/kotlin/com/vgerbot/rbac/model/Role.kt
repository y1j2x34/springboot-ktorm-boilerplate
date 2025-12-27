package com.vgerbot.rbac.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface Role : Entity<Role> {
    companion object : Entity.Factory<Role>()
    
    val id: Int
    var name: String
    var code: String
    var description: String?
    var createdAt: Instant
    var updatedAt: Instant?
}

object Roles : Table<Role>("role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val description = varchar("description").bindTo { it.description }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
}

val Database.roles get() = this.sequenceOf(Roles)

