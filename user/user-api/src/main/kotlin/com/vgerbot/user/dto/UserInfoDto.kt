package com.vgerbot.user.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

data class UserInfoDto (
    val id: Int,

    val username: String,
    @JsonIgnore
    val password: String,

    val email: String,

    val phoneNumber: String?,

    val createdAt: Instant
)