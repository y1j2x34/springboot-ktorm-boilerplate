package com.vgerbot.oauth2server.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.vgerbot.oauth2server.dao.OAuth2ClientDao
import com.vgerbot.oauth2server.entity.OAuth2Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.*

/**
 * OAuth2 Authorization Server 配置
 */
@Configuration
@EnableWebSecurity
class OAuth2AuthorizationServerConfig(
    private val oauth2ClientDao: OAuth2ClientDao,
    private val properties: OAuth2AuthorizationServerProperties
) {

    /**
     * OAuth2 Authorization Server Security Filter Chain
     * 配置授权服务器的安全过滤器链
     */
    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
        
        http
            .exceptionHandling { exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt(Customizer.withDefaults())
            }
        
        return http.build()
    }

    /**
     * Registered Client Repository
     * 从数据库读取已注册的客户端
     */
    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        return object : RegisteredClientRepository {
            override fun save(registeredClient: RegisteredClient) {
                // 客户端通过管理 API 创建，这里不需要实现保存
                throw UnsupportedOperationException("Client registration should be done through management API")
            }

            override fun findById(id: String): RegisteredClient? {
                val client = oauth2ClientDao.findById(id.toInt())
                return client?.let { toRegisteredClient(it) }
            }

            override fun findByClientId(clientId: String): RegisteredClient? {
                val client = oauth2ClientDao.findByClientId(clientId)
                return client?.let { toRegisteredClient(it) }
            }
        }
    }

    /**
     * 将数据库实体转换为 RegisteredClient
     */
    private fun toRegisteredClient(client: OAuth2Client): RegisteredClient {
        val clientAuthenticationMethods = client.clientAuthenticationMethods
            .split(",")
            .map { it.trim() }
            .map { method ->
                when (method) {
                    "client_secret_basic" -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                    "client_secret_post" -> ClientAuthenticationMethod.CLIENT_SECRET_POST
                    "private_key_jwt" -> ClientAuthenticationMethod.PRIVATE_KEY_JWT
                    "none" -> ClientAuthenticationMethod.NONE
                    else -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                }
            }
            .toSet()

        val authorizationGrantTypes = client.authorizationGrantTypes
            .split(",")
            .map { it.trim() }
            .map { grantType ->
                when (grantType) {
                    "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE
                    "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN
                    "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS
                    else -> AuthorizationGrantType(grantType)
                }
            }
            .toSet()

        val redirectUris = client.redirectUris
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()

        val scopes = client.scopes
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: setOf(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)

        val clientSettings = ClientSettings.builder()
            .requireProofKey(client.requireProofKey)
            .build()

        val tokenSettings = TokenSettings.builder()
            .accessTokenTimeToLive(
                Duration.ofSeconds(client.accessTokenValiditySeconds?.toLong() ?: 3600)
            )
            .refreshTokenTimeToLive(
                Duration.ofSeconds(client.refreshTokenValiditySeconds?.toLong() ?: 604800)
            )
            .reuseRefreshTokens(true)
            .build()

        return RegisteredClient.withId(client.id.toString())
            .clientId(client.clientId)
            .clientSecret(client.clientSecret)
            .clientAuthenticationMethods { it.addAll(clientAuthenticationMethods) }
            .authorizationGrantTypes { it.addAll(authorizationGrantTypes) }
            .redirectUris { it.addAll(redirectUris) }
            .scopes { it.addAll(scopes) }
            .clientSettings(clientSettings)
            .tokenSettings(tokenSettings)
            .build()
    }

    /**
     * JWK Source
     * 用于签名和验证 JWT Token
     */
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val rsaKey = RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    /**
     * 生成 RSA 密钥对
     */
    private fun generateRsaKey(): KeyPair {
        val keyPair = KeyPairGenerator.getInstance("RSA")
        keyPair.initialize(2048)
        return keyPair.generateKeyPair()
    }

    /**
     * JWT Decoder
     * 用于解码和验证 JWT Token
     * 注意：这个 Bean 主要用于资源服务器，授权服务器会自动处理 JWT
     */
    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSource(jwkSource).build()
    }

    /**
     * Authorization Server Settings
     * 配置授权服务器设置
     */
    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .issuer(properties.issuer)
            .authorizationEndpoint(properties.authorizationEndpoint)
            .tokenEndpoint(properties.tokenEndpoint)
            .tokenRevocationEndpoint(properties.tokenRevocationEndpoint)
            .tokenIntrospectionEndpoint(properties.tokenIntrospectionEndpoint)
            .jwkSetEndpoint(properties.jwkSetEndpoint)
            .oidcUserInfoEndpoint(properties.oidcUserInfoEndpoint)
            .oidcClientRegistrationEndpoint(properties.oidcClientRegistrationEndpoint)
            .build()
    }
}

