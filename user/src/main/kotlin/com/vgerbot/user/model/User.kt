package com.vgerbot.user.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

interface User: Entity<User> {
    companion object : Entity.Factory<User>()

    val id: Int

    var username: String

    var email: String

    var password: String

    var createdAt: Instant
}

object Users: Table<User>("user") {
    val id = int("id").primaryKey().bindTo { it.id }

    var username = varchar("username").bindTo { it.username }

    var email = varchar("email").bindTo { it.email }

    var password = varchar("password").bindTo { it.password }

    var createdAt = timestamp("created_at").bindTo { it.createdAt }

}

val Database.users get() = this.sequenceOf(Users)
