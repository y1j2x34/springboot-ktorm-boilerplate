package com.vgerbot.oauth2server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * OAuth2 Authorization Server 配置属性
 */
@ConfigurationProperties(prefix = "oauth2.authorization-server", ignoreInvalidFields = false)
data class OAuth2AuthorizationServerProperties(
    /**
     * 授权服务器发行者 URI
     */
    val issuer: String = "http://localhost:8081/api",
    
    /**
     * 授权端点路径
     */
    val authorizationEndpoint: String = "/oauth2/authorize",
    
    /**
     * Token 端点路径
     */
    val tokenEndpoint: String = "/oauth2/token",
    
    /**
     * Token 撤销端点路径
     */
    val tokenRevocationEndpoint: String = "/oauth2/revoke",
    
    /**
     * Token 内省端点路径
     */
    val tokenIntrospectionEndpoint: String = "/oauth2/introspect",
    
    /**
     * JWK Set 端点路径
     */
    val jwkSetEndpoint: String = "/oauth2/jwks",
    
    /**
     * OIDC 用户信息端点路径
     */
    val oidcUserInfoEndpoint: String = "/userinfo",
    
    /**
     * OIDC 客户端注册端点路径
     */
    val oidcClientRegistrationEndpoint: String = "/connect/register"
)

@Configuration
@EnableConfigurationProperties(OAuth2AuthorizationServerProperties::class)
class OAuth2AuthorizationServerPropertiesConfiguration

