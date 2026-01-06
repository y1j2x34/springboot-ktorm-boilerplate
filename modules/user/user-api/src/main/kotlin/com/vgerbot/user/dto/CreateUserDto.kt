package com.vgerbot.user.dto

data class CreateUserDto(
    val username: String,
    val email: String,
    // MD5(plaintext password)
    val password: String,
    val phoneNumber: String? = null
)
