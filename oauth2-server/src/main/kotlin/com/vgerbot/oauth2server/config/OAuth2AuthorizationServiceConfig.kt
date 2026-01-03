package com.vgerbot.oauth2server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository

/**
 * OAuth2 Authorization Service 配置
 * 
 * 使用 JDBC 存储授权信息（授权码、访问令牌等）
 */
@Configuration
class OAuth2AuthorizationServiceConfig {

    /**
     * OAuth2 Authorization Service
     * 使用 Spring Authorization Server 提供的 JDBC 实现
     */
    @Bean
    fun oauth2AuthorizationService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
    }
}

