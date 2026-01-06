package com.vgerbot.oauth2server.data

/**
 * OAuth2 客户端创建请求
 */
data class OAuth2ClientCreateRequest(
    val clientId: String,
    val clientSecret: String,
    val clientName: String,
    val description: String? = null,
    val clientAuthenticationMethods: List<String> = listOf("client_secret_basic"),
    val authorizationGrantTypes: List<String> = listOf("authorization_code", "refresh_token"),
    val redirectUris: List<String>? = null,
    val scopes: List<String>? = null,
    val requireProofKey: Boolean = false,
    val accessTokenValiditySeconds: Int? = 3600,
    val refreshTokenValiditySeconds: Int? = 604800,
    val enabled: Boolean = true
)

