package com.vgerbot.authorization.entity

import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface UserRole : SimpleAuditableEntity<UserRole> {
    companion object : Entity.Factory<UserRole>()
    
    val id: Int
    var userId: Int
    var roleId: Int
}

object UserRoles : SimpleAuditableTable<UserRole>("user_role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val roleId = int("role_id").bindTo { it.roleId }
}

val Database.userRoles get() = this.sequenceOf(UserRoles)

