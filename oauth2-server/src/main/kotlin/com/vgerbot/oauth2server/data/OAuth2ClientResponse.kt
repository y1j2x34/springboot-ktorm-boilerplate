package com.vgerbot.oauth2server.data

import java.time.Instant

/**
 * OAuth2 客户端响应
 */
data class OAuth2ClientResponse(
    val id: Int,
    val clientId: String,
    val clientName: String,
    val description: String?,
    val clientAuthenticationMethods: List<String>,
    val authorizationGrantTypes: List<String>,
    val redirectUris: List<String>?,
    val scopes: List<String>?,
    val requireProofKey: Boolean,
    val accessTokenValiditySeconds: Int?,
    val refreshTokenValiditySeconds: Int?,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

