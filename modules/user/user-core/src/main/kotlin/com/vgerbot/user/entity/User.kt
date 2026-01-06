package com.vgerbot.user.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

interface User: Entity<User> {
    companion object : Entity.Factory<User>()

    val id: Int

    var username: String

    var email: String

    var phoneNumber: String?

    var password: String

    var createdAt: Instant

    var updatedAt: Instant?

    var isDeleted: Boolean
}

object Users: Table<User>("user") {
    val id = int("id").primaryKey().bindTo { it.id }

    var username = varchar("username").bindTo { it.username }

    var email = varchar("email").bindTo { it.email }

    var phoneNumber = varchar("phone_number").bindTo { it.phoneNumber }

    var password = varchar("password").bindTo { it.password }

    var createdAt = timestamp("created_at").bindTo { it.createdAt }

    var updatedAt = timestamp("updated_at").bindTo { it.updatedAt }

    var isDeleted = boolean("is_deleted").bindTo { it.isDeleted }

}

val Database.users get() = this.sequenceOf(Users)

