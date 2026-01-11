package com.vgerbot.auth.data

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 公钥响应 DTO
 */
data class PublicKeyResponse(
    @JsonProperty("key_id")
    val keyId: String,
    
    @JsonProperty("public_key")
    val publicKey: String,
    
    @JsonProperty("expires_at")
    val expiresAt: Long,
    
    @JsonProperty("algorithm")
    val algorithm: String
)

