package com.vgerbot.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * OAuth2 Provider DTO（用于创建和更新）
 */
data class CreateOAuth2ProviderDto(
    @field:NotBlank(message = "Registration ID is required")
    @field:Size(max = 50, message = "Registration ID must be at most 50 characters")
    @JsonProperty("registration_id")
    val registrationId: String,
    
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String,
    
    @field:NotBlank(message = "Client ID is required")
    @field:Size(max = 255, message = "Client ID must be at most 255 characters")
    @JsonProperty("client_id")
    val clientId: String,
    
    @field:NotBlank(message = "Client Secret is required")
    @field:Size(max = 500, message = "Client Secret must be at most 500 characters")
    @JsonProperty("client_secret")
    val clientSecret: String,
    
    @JsonProperty("authorization_uri")
    val authorizationUri: String? = null,
    
    @JsonProperty("token_uri")
    val tokenUri: String? = null,
    
    @JsonProperty("user_info_uri")
    val userInfoUri: String? = null,
    
    @JsonProperty("jwk_set_uri")
    val jwkSetUri: String? = null,
    
    @JsonProperty("issuer_uri")
    val issuerUri: String? = null,
    
    @JsonProperty("redirect_uri")
    val redirectUri: String? = null,
    
    @field:NotBlank(message = "Scopes is required")
    val scopes: String = "openid,profile,email",
    
    @JsonProperty("user_name_attribute_name")
    val userNameAttributeName: String = "sub",

    @JsonProperty("client_authentication_method")
    val clientAuthenticationMethod: String = "client_secret_basic",

    @JsonProperty("authorization_grant_type")
    val authorizationGrantType: String = "authorization_code",

    @JsonProperty("authorization_request_params")
    val authorizationRequestParams: Map<String, Any>? = null,

    @JsonProperty("id_token_jws_algorithm")
    val idTokenJwsAlgorithm: String? = null,
    
    val status: Int = 1,
    
    @JsonProperty("sort_order")
    val sortOrder: Int = 0,
    
    val description: String? = null,
    
    /** 租户ID（可选，支持多租户配置隔离） */
    @JsonProperty("tenant_id")
    val tenantId: Int? = null
)

/**
 * OAuth2 Provider 更新 DTO
 */
data class UpdateOAuth2ProviderDto(
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String? = null,
    
    @field:Size(max = 255, message = "Client ID must be at most 255 characters")
    @JsonProperty("client_id")
    val clientId: String? = null,
    
    @field:Size(max = 500, message = "Client Secret must be at most 500 characters")
    @JsonProperty("client_secret")
    val clientSecret: String? = null,
    
    @JsonProperty("authorization_uri")
    val authorizationUri: String? = null,
    
    @JsonProperty("token_uri")
    val tokenUri: String? = null,
    
    @JsonProperty("user_info_uri")
    val userInfoUri: String? = null,
    
    @JsonProperty("jwk_set_uri")
    val jwkSetUri: String? = null,
    
    @JsonProperty("issuer_uri")
    val issuerUri: String? = null,
    
    @JsonProperty("redirect_uri")
    val redirectUri: String? = null,
    
    val scopes: String? = null,
    
    @JsonProperty("user_name_attribute_name")
    val userNameAttributeName: String? = null,

    @JsonProperty("client_authentication_method")
    val clientAuthenticationMethod: String? = null,

    @JsonProperty("authorization_grant_type")
    val authorizationGrantType: String? = null,

    @JsonProperty("authorization_request_params")
    val authorizationRequestParams: Map<String, Any>? = null,

    @JsonProperty("id_token_jws_algorithm")
    val idTokenJwsAlgorithm: String? = null,
    
    val status: Int? = null,
    
    @JsonProperty("sort_order")
    val sortOrder: Int? = null,
    
    val description: String? = null,
    
    /** 租户ID（可选，支持多租户配置隔离） */
    @JsonProperty("tenant_id")
    val tenantId: Int? = null
)

/**
 * OAuth2 Provider 响应 DTO
 */
data class OAuth2ProviderResponseDto(
    val id: Int,
    
    @JsonProperty("registration_id")
    val registrationId: String,
    
    val name: String,
    
    @JsonProperty("client_id")
    val clientId: String,
    
    // 注意：client_secret 不在响应中返回
    
    @JsonProperty("authorization_uri")
    val authorizationUri: String?,
    
    @JsonProperty("token_uri")
    val tokenUri: String?,
    
    @JsonProperty("user_info_uri")
    val userInfoUri: String?,
    
    @JsonProperty("jwk_set_uri")
    val jwkSetUri: String?,
    
    @JsonProperty("issuer_uri")
    val issuerUri: String?,
    
    @JsonProperty("redirect_uri")
    val redirectUri: String?,
    
    val scopes: List<String>,
    
    @JsonProperty("user_name_attribute_name")
    val userNameAttributeName: String,

    @JsonProperty("client_authentication_method")
    val clientAuthenticationMethod: String,

    @JsonProperty("authorization_grant_type")
    val authorizationGrantType: String,

    @JsonProperty("authorization_request_params")
    val authorizationRequestParams: Map<String, Any>?,

    @JsonProperty("id_token_jws_algorithm")
    val idTokenJwsAlgorithm: String?,
    
    val status: Int,
    
    @JsonProperty("sort_order")
    val sortOrder: Int,
    
    val description: String?,
    
    @JsonProperty("tenant_id")
    val tenantId: Int?,
    
    @JsonProperty("created_at")
    val createdAt: String,
    
    @JsonProperty("updated_at")
    val updatedAt: String?
)

