package com.vgerbot.user.dto

import java.time.Instant

data class UserInfoDto (
    val id: Int,

    var username: String,

    var email: String,

    var phoneNumber: String?,

    var createdAt: Instant
)