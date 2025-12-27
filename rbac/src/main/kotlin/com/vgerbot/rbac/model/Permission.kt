package com.vgerbot.rbac.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface Permission : Entity<Permission> {
    companion object : Entity.Factory<Permission>()
    
    val id: Int
    var name: String
    var code: String
    var resource: String
    var action: String
    var description: String?
    var createdAt: Instant
}

object Permissions : Table<Permission>("permission") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val resource = varchar("resource").bindTo { it.resource }
    val action = varchar("action").bindTo { it.action }
    val description = varchar("description").bindTo { it.description }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
}

val Database.permissions get() = this.sequenceOf(Permissions)

