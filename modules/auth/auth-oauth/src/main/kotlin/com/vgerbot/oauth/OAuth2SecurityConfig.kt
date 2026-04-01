package com.vgerbot.oauth

import com.vgerbot.oauth.dao.OAuth2ProviderDao
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoderFactory

/**
 * OAuth2 Security 配置
 * 
 * 配置 OAuth2 登录相关的安全设置
 * 这个配置会被应用层的 SecurityConfiguration 使用
 */
@Configuration
@ConditionalOnProperty(prefix = "oauth2", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OAuth2SecurityConfig(
    private val authorizationRequestResolver: DatabaseBackedOAuth2AuthorizationRequestResolver,
    private val oauth2ProviderDao: OAuth2ProviderDao,
    private val oauth2SuccessHandler: OAuth2SuccessHandler,
    private val oauth2FailureHandler: OAuth2FailureHandler
) {
    
    /**
     * 配置 OAuth2 登录
     * 
     * 这个方法可以被应用层的 SecurityConfiguration 调用
     * 来配置 OAuth2 登录
     */
    fun configureOAuth2Login(oauth2Login: OAuth2LoginConfigurer<HttpSecurity>) {
        oauth2Login
            .loginPage("/oauth2/authorization/logto")
            .authorizationEndpoint {
                it.authorizationRequestResolver(authorizationRequestResolver)
            }
            .successHandler(oauth2SuccessHandler)
            .failureHandler(oauth2FailureHandler)
    }

    @Bean
    fun idTokenDecoderFactory(): JwtDecoderFactory<ClientRegistration> {
        return JwtDecoderFactory { clientRegistration ->
            val factory = OidcIdTokenDecoderFactory()
            oauth2ProviderDao.findByRegistrationId(clientRegistration.registrationId)
                ?.idTokenJwsAlgorithm
                ?.takeIf { it.isNotBlank() }
                ?.let { algorithm ->
                    factory.setJwsAlgorithmResolver {
                        SignatureAlgorithm.valueOf(algorithm.uppercase())
                    }
                }
            factory.createDecoder(clientRegistration)
        }
    }
}

