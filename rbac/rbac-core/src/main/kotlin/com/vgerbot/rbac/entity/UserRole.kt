package com.vgerbot.rbac.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface UserRole : Entity<UserRole> {
    companion object : Entity.Factory<UserRole>()
    
    val id: Int
    var userId: Int
    var roleId: Int
    var createdAt: Instant
}

object UserRoles : Table<UserRole>("user_role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val roleId = int("role_id").bindTo { it.roleId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
}

val Database.userRoles get() = this.sequenceOf(UserRoles)

