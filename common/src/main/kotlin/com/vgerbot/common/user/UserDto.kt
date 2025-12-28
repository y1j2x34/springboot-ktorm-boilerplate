package com.vgerbot.common.user

import java.time.Instant

data class UserDto (
    val id: Int,

    var username: String,

    var email: String,

    var createdAt: Instant
)