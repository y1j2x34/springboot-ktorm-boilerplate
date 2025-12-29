package com.vgerbot.app.configuration

import com.vgerbot.auth.CustomUserDetailsService
import com.vgerbot.auth.JwtAuthenticationEntryPoint
import com.vgerbot.auth.JwtRequestFilter
import com.vgerbot.com.vgerbot.tenant.filter.TenantAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 应用层 Security 配置
 * 
 * 这个配置是应用的主 Security 配置，负责：
 * 1. 定义 Security 过滤器链和访问控制规则
 * 2. 集成 JWT 认证（来自 jwt-auth 模块）
 * 3. 集成多租户支持（来自 tenant 模块）
 * 4. 配置认证提供者和认证管理器
 * 
 * JWT 相关的核心组件（JwtRequestFilter、JwtTokenUtils 等）由 jwt-auth 模块提供。
 * 该配置类负责将这些组件组装到应用的 Security 配置中。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class AppSecurityConfiguration {
    
    @Autowired
    lateinit var jwtRequestFilter: JwtRequestFilter
    
    @Autowired
    lateinit var tenantAuthenticationFilter: TenantAuthenticationFilter

    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService

    @Autowired
    lateinit var unauthorizedHandler: JwtAuthenticationEntryPoint

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    /**
     * 配置 Security 过滤器链
     * 
     * 过滤器执行顺序：
     * 1. JwtRequestFilter - 解析 JWT Token，完成基础认证，设置 SecurityContext
     * 2. TenantAuthenticationFilter - 在认证之后，从 Token 中提取并注入租户信息
     * 
     * 访问控制规则：
     * - /public/ ** - 公开访问（登录、注册等）
     * - 其他所有请求 - 需要认证
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity, daoAuthenticationProvider: DaoAuthenticationProvider): DefaultSecurityFilterChain =
        http
            // 禁用 CORS 和 CSRF（前后端分离应用通常使用 JWT，不需要 CSRF 保护）
            .cors { it.disable() }
            .csrf { it.disable() }
            // 配置异常处理（未认证时返回 401）
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedHandler) }
            // 配置访问控制规则
            .authorizeHttpRequests { authorize ->
                // 公开端点（不需要认证）
                authorize.requestMatchers(HttpMethod.POST, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.PUT, "/public/**").permitAll()
                // 其他所有端点都需要认证
                authorize.anyRequest().authenticated()
            }
            // 设置认证提供者
            .authenticationProvider(daoAuthenticationProvider)
            // 添加 JWT 认证过滤器（在标准的用户名密码认证过滤器之前）
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            // 添加租户认证过滤器（在 JWT 过滤器之后，确保已经完成基础认证）
            .addFilterAfter(tenantAuthenticationFilter, JwtRequestFilter::class.java)
            .build()

//    /**
//     * 配置 DAO 认证提供者
//     *
//     * 用于基于用户名和密码的认证。
//     * 在登录时，AuthenticationManager 会使用这个 Provider 来验证用户凭证。
//     */
//    @Bean
//    fun daoAuthenticationProvider(): DaoAuthenticationProvider {
//        val authProvider = DaoAuthenticationProvider()
//        authProvider.setUserDetailsService(userDetailsService)
//        authProvider.setPasswordEncoder(passwordEncoder)
//        return authProvider
//    }

    /**
     * 提供 AuthenticationManager Bean
     * 
     * 在 AuthController 中使用，用于执行登录认证。
     * 该 Bean 覆盖了 jwt-auth 模块中的默认配置。
     */
    @Bean
    fun authenticationManager(authConfiguration: AuthenticationConfiguration) = 
        authConfiguration.authenticationManager
}

