package com.vgerbot.oauth

import com.vgerbot.oauth.dao.OAuth2ProviderDao
import com.vgerbot.oauth.entity.OAuth2Provider
import com.vgerbot.oauth.service.ClientRegistrationRefresher
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrations
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 动态 OAuth2 客户端注册仓库
 * 
 * 从数据库读取 OAuth2 提供商配置，支持运行时动态更新
 */
@Component
@ConditionalOnProperty(prefix = "oauth2", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class DynamicClientRegistrationRepository(
    private val oauth2ProviderDao: OAuth2ProviderDao,
    private val oauth2Properties: OAuth2Properties
) : ClientRegistrationRepository, ClientRegistrationRefresher {
    
    private val logger = LoggerFactory.getLogger(DynamicClientRegistrationRepository::class.java)
    
    // 缓存客户端注册
    private val registrationCache = ConcurrentHashMap<String, ClientRegistration>()
    
    // 初始化标志
    @Volatile
    private var initialized = false
    
    /**
     * 获取客户端注册
     */
    override fun findByRegistrationId(registrationId: String): ClientRegistration? {
        ensureInitialized()
        
        // 优先从缓存获取
        registrationCache[registrationId]?.let { return it }
        
        // 从数据库加载
        val provider = oauth2ProviderDao.findByRegistrationId(registrationId)
            ?: return null
        
        val registration = buildClientRegistration(provider)
        registrationCache[registrationId] = registration
        
        return registration
    }
    
    /**
     * 刷新缓存
     */
    override fun refresh() {
        logger.info("Refreshing OAuth2 client registrations...")
        
        registrationCache.clear()
        
        try {
            val providers = oauth2ProviderDao.findAllEnabled()
            providers.forEach { provider ->
                try {
                    val registration = buildClientRegistration(provider)
                    registrationCache[provider.registrationId] = registration
                    logger.debug("Loaded OAuth2 provider: ${provider.registrationId}")
                } catch (e: Exception) {
                    logger.error("Failed to build ClientRegistration for ${provider.registrationId}", e)
                }
            }
            
            initialized = true
            logger.info("Loaded ${registrationCache.size} OAuth2 client registrations")
        } catch (e: Exception) {
            logger.error("Failed to refresh OAuth2 client registrations", e)
        }
    }
    
    /**
     * 确保已初始化
     */
    private fun ensureInitialized() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    refresh()
                }
            }
        }
    }
    
    /**
     * 获取所有客户端注册
     */
    fun findAll(): List<ClientRegistration> {
        ensureInitialized()
        return registrationCache.values.toList()
    }
    
    /**
     * 构建客户端注册
     */
    private fun buildClientRegistration(provider: OAuth2Provider): ClientRegistration {
        val scopes = provider.scopes
            .ifBlank { oauth2Properties.defaultScopes }
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val resolvedUserNameAttributeName = provider.userNameAttributeName.ifBlank {
            oauth2Properties.defaultUserNameAttribute
        }

        val builder = if (!provider.issuerUri.isNullOrBlank()) {
            // OIDC discovery fills authorization/token/jwk/userinfo endpoints from issuer metadata.
            ClientRegistrations.fromOidcIssuerLocation(provider.issuerUri)
                .registrationId(provider.registrationId)
        } else {
            ClientRegistration.withRegistrationId(provider.registrationId)
        }
            .clientId(provider.clientId)
            .clientSecret(provider.clientSecret)
            .clientAuthenticationMethod(resolveClientAuthenticationMethod(provider.clientAuthenticationMethod))
            .authorizationGrantType(resolveAuthorizationGrantType(provider.authorizationGrantType))
            .redirectUri(provider.redirectUri ?: oauth2Properties.defaultRedirectUri)
            .scope(*scopes.toTypedArray())
            .clientName(provider.name)

        // Manual endpoint values override discovery results when explicitly provided.
        provider.authorizationUri?.let {
            if (it.isNotBlank()) builder.authorizationUri(it)
        }

        provider.tokenUri?.let {
            if (it.isNotBlank()) builder.tokenUri(it)
        }

        provider.userInfoUri?.let {
            if (it.isNotBlank()) builder.userInfoUri(it)
        }

        provider.jwkSetUri?.let {
            if (it.isNotBlank()) builder.jwkSetUri(it)
        }

        builder.userNameAttributeName(resolvedUserNameAttributeName)

        return builder.build()
    }

    private fun resolveClientAuthenticationMethod(value: String): ClientAuthenticationMethod {
        return when (value.lowercase()) {
            "client_secret_post" -> ClientAuthenticationMethod.CLIENT_SECRET_POST
            "client_secret_jwt" -> ClientAuthenticationMethod.CLIENT_SECRET_JWT
            "private_key_jwt" -> ClientAuthenticationMethod.PRIVATE_KEY_JWT
            "none" -> ClientAuthenticationMethod.NONE
            else -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC
        }
    }

    private fun resolveAuthorizationGrantType(value: String): AuthorizationGrantType {
        return when (value.lowercase()) {
            "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS
            "password" -> AuthorizationGrantType.PASSWORD
            "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN
            else -> AuthorizationGrantType.AUTHORIZATION_CODE
        }
    }
}

/**
 * OAuth2 客户端注册配置
 * 
 * 提供 ClientRegistrationRepository Bean
 */
@Configuration
@ConditionalOnProperty(prefix = "oauth2", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OAuth2ClientRegistrationConfig {
    
    /**
     * 创建客户端注册仓库
     */
    @Bean
    @Primary
    fun clientRegistrationRepository(
        dynamicClientRegistrationRepository: DynamicClientRegistrationRepository
    ): ClientRegistrationRepository {
        return dynamicClientRegistrationRepository
    }
    
    /**
     * 创建客户端注册刷新器
     */
    @Bean
    fun clientRegistrationRefresher(
        dynamicClientRegistrationRepository: DynamicClientRegistrationRepository
    ): ClientRegistrationRefresher {
        return dynamicClientRegistrationRepository
    }
}
