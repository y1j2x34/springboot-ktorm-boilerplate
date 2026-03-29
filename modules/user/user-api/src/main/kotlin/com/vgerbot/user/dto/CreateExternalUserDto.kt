package com.vgerbot.user.dto

/**
 * 外部认证用户创建请求。
 */
data class CreateExternalUserDto(
    val username: String,
    val email: String,
    val authProvider: String,
    val externalId: String,
    val phoneNumber: String? = null
)
