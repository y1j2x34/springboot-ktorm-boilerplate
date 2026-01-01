package com.vgerbot.user.dto

import java.time.Instant

data class UserInfoDto (
    val id: Int,

    val username: String,

    val password: String,

    val email: String,

    val phoneNumber: String?,

    val createdAt: Instant
)