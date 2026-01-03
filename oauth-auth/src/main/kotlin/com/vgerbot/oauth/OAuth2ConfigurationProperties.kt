package com.vgerbot.oauth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * OAuth2/OIDC 配置属性
 * 
 * 注意：OAuth2 提供商配置现在从数据库读取，不再从配置文件读取。
 * 使用 OAuth2ProviderService 和管理 API 来管理提供商配置。
 * 
 * @property enabled 是否启用 OAuth2 认证
 * @property defaultRedirectUri 默认重定向 URI 模板
 * @property defaultScopes 默认权限范围
 * @property defaultUserNameAttribute 默认用户名属性名
 */
@ConfigurationProperties(prefix = "oauth2", ignoreInvalidFields = false)
data class OAuth2Properties(
    val enabled: Boolean = true,
    val defaultRedirectUri: String = "{baseUrl}/login/oauth2/code/{registrationId}",
    val defaultScopes: String = "openid,profile,email",
    val defaultUserNameAttribute: String = "sub"
)

@Configuration
@EnableConfigurationProperties(OAuth2Properties::class)
class OAuth2ConfigurationProperties

