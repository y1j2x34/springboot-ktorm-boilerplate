package com.vgerbot.oauth2server.data

/**
 * OAuth2 客户端更新请求
 */
data class OAuth2ClientUpdateRequest(
    val clientName: String? = null,
    val description: String? = null,
    val clientAuthenticationMethods: List<String>? = null,
    val authorizationGrantTypes: List<String>? = null,
    val redirectUris: List<String>? = null,
    val scopes: List<String>? = null,
    val requireProofKey: Boolean? = null,
    val accessTokenValiditySeconds: Int? = null,
    val refreshTokenValiditySeconds: Int? = null,
    val enabled: Boolean? = null
)

