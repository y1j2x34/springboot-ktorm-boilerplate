package com.vgerbot.user.dto

/**
 * 绑定外部身份请求。
 */
data class BindExternalIdentityDto(
    val authProvider: String,
    val externalId: String
)
