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
    private val oauth2ProviderDao: OAuth2ProviderDao
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
        val scopes = provider.scopes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        // 如果提供了 issuerUri，使用自动发现配置
        if (!provider.issuerUri.isNullOrBlank()) {
            return ClientRegistration.withRegistrationId(provider.registrationId)
                .issuerUri(provider.issuerUri)
                .clientId(provider.clientId)
                .clientSecret(provider.clientSecret)
                .redirectUri(provider.redirectUri ?: "{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(*scopes.toTypedArray())
                .clientName(provider.name)
                .build()
        }
        
        // 手动配置各个端点
        val builder = ClientRegistration.withRegistrationId(provider.registrationId)
            .clientId(provider.clientId)
            .clientSecret(provider.clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(provider.redirectUri ?: "{baseUrl}/login/oauth2/code/{registrationId}")
            .scope(*scopes.toTypedArray())
            .clientName(provider.name)
        
        // 设置授权端点
        provider.authorizationUri?.let {
            if (it.isNotBlank()) builder.authorizationUri(it)
        }
        
        // 设置 Token 端点
        provider.tokenUri?.let {
            if (it.isNotBlank()) builder.tokenUri(it)
        }
        
        // 设置用户信息端点
        provider.userInfoUri?.let {
            if (it.isNotBlank()) builder.userInfoUri(it)
        }
        
        // 设置 JWK Set URI
        provider.jwkSetUri?.let {
            if (it.isNotBlank()) builder.jwkSetUri(it)
        }
        
        // 设置用户名属性名
        builder.userNameAttributeName(provider.userNameAttributeName)
        
        return builder.build()
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
