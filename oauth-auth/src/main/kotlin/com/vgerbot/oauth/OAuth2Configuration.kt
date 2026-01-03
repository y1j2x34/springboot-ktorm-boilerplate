package com.vgerbot.oauth

import com.vgerbot.oauth.service.CustomOAuth2UserService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * OAuth2/OIDC 配置类
 * 
 * 配置 OAuth2 客户端和用户信息服务
 */
@Configuration
@ConditionalOnProperty(prefix = "oauth2", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OAuth2Configuration {
    
    /**
     * 配置 OAuth2 User Service
     * 使用自定义服务来处理 OAuth2 用户信息
     */
    @Bean
    fun oauth2UserService(customOAuth2UserService: CustomOAuth2UserService): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
        return OAuth2UserService { request ->
            customOAuth2UserService.loadOAuth2User(request)
        }
    }
    
    /**
     * 配置 OIDC User Service
     * 使用自定义服务来处理 OIDC 用户信息（包含 ID Token）
     */
    @Bean
    fun oidcUserService(customOAuth2UserService: CustomOAuth2UserService): OidcUserService {
        return CustomOidcUserService(customOAuth2UserService)
    }
    
    /**
     * 自定义 OIDC User Service 实现
     */
    private class CustomOidcUserService(
        private val customOAuth2UserService: CustomOAuth2UserService
    ) : OidcUserService() {
        override fun loadUser(userRequest: OidcUserRequest): OidcUser {
            return customOAuth2UserService.loadOidcUser(userRequest)
        }
    }
}

