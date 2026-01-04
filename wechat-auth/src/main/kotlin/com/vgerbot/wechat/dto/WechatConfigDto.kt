package com.vgerbot.wechat.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 微信配置创建 DTO
 */
data class CreateWechatConfigDto(
    @field:NotBlank(message = "Config ID is required")
    @field:Size(max = 50, message = "Config ID must be at most 50 characters")
    @JsonProperty("config_id")
    val configId: String,
    
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String,
    
    @field:NotBlank(message = "Login type is required")
    @JsonProperty("login_type")
    val loginType: String, // OPEN_PLATFORM, MP, MINI_PROGRAM
    
    @field:NotBlank(message = "AppID is required")
    @field:Size(max = 100, message = "AppID must be at most 100 characters")
    @JsonProperty("app_id")
    val appId: String,
    
    @field:NotBlank(message = "AppSecret is required")
    @field:Size(max = 200, message = "AppSecret must be at most 200 characters")
    @JsonProperty("app_secret")
    val appSecret: String,
    
    /** Token（公众号验证用，可选） */
    val token: String? = null,
    
    /** EncodingAESKey（消息加解密密钥，可选） */
    @JsonProperty("encoding_aes_key")
    val encodingAesKey: String? = null,
    
    val status: Int = 1,
    
    @JsonProperty("sort_order")
    val sortOrder: Int = 0,
    
    val description: String? = null
)

/**
 * 微信配置更新 DTO
 */
data class UpdateWechatConfigDto(
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String? = null,
    
    @JsonProperty("app_id")
    val appId: String? = null,
    
    @JsonProperty("app_secret")
    val appSecret: String? = null,
    
    val token: String? = null,
    
    @JsonProperty("encoding_aes_key")
    val encodingAesKey: String? = null,
    
    val status: Int? = null,
    
    @JsonProperty("sort_order")
    val sortOrder: Int? = null,
    
    val description: String? = null
)

/**
 * 微信配置响应 DTO
 */
data class WechatConfigResponseDto(
    val id: Int,
    
    @JsonProperty("config_id")
    val configId: String,
    
    val name: String,
    
    @JsonProperty("login_type")
    val loginType: String,
    
    @JsonProperty("app_id")
    val appId: String,
    
    // 注意：app_secret 不在响应中返回
    
    val status: Int,
    
    @JsonProperty("sort_order")
    val sortOrder: Int,
    
    val description: String?,
    
    @JsonProperty("login_url")
    val loginUrl: String,
    
    @JsonProperty("created_at")
    val createdAt: String,
    
    @JsonProperty("updated_at")
    val updatedAt: String?
)

/**
 * 微信登录回调请求
 */
data class WechatLoginCallbackRequest(
    /** 授权码 */
    val code: String,
    
    /** 状态参数（防 CSRF） */
    val state: String? = null
)

/**
 * 微信用户信息
 */
data class WechatUserInfo(
    val openid: String,
    val unionid: String?,
    val nickname: String?,
    val headimgurl: String?,
    val sex: Int?,
    val province: String?,
    val city: String?,
    val country: String?
)

