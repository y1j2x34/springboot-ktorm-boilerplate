package com.vgerbot.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * JWT 模块配置
 * 
 * 该配置类仅提供 JWT 相关的辅助 Bean。
 * 核心 JWT 组件（JwtRequestFilter、JwtAuthenticationEntryPoint、JwtTokenUtils 等）
 * 已经通过 @Component 注解自动注册。
 * 
 * Security 配置（过滤器链、访问控制等）应该由应用层的 SecurityConfiguration 提供。
 */
@Configuration
class JwtConfiguration {

    /**
     * 提供 AuthenticationManager Bean
     * 用于在 AuthController 等地方进行认证
     */
    @Bean
    @ConditionalOnMissingBean
    fun authenticationManager(authConfiguration: AuthenticationConfiguration) = 
        authConfiguration.authenticationManager

    /**
     * 提供 DaoAuthenticationProvider Bean
     * 用于基于用户名密码的认证
     *
     */
    @Bean
    fun daoAuthenticationProvider(
        userDetailsService: CustomUserDetailsService,
        passwordEncoder: PasswordEncoder
    ): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }
}