package com.vgerbot.auth.data

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Token 响应 DTO
 */
data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    
    @JsonProperty("refresh_token")
    val refreshToken: String,
    
    @JsonProperty("token_type")
    val tokenType: String = "Bearer",
    
    @JsonProperty("expires_in")
    val expiresIn: Long,
    
    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Long
)

/**
 * 刷新 Token 请求
 */
data class RefreshTokenRequest(
    @JsonProperty("refresh_token")
    val refreshToken: String
)

/**
 * Token 类型枚举
 */
enum class TokenType {
    ACCESS,
    REFRESH
}

