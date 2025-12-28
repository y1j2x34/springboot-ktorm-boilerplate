package com.vgerbot.app.configuration

import com.vgerbot.auth.CustomUserDetailsService
import com.vgerbot.auth.JwtAuthenticationEntryPoint
import com.vgerbot.auth.JwtRequestFilter
import com.vgerbot.tenant.filter.TenantAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 应用层 Security 配置
 * 
 * 这个配置覆盖了 jwt-auth 模块中的 JwtConfiguration，
 * 在其基础上添加了 tenant 模块的支持
 * 
 * 通过这种方式，我们可以在不修改 jwt-auth 和 user 模块的情况下集成 tenant 功能
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
     * 顺序：
     * 1. JwtRequestFilter - 解析 JWT Token，完成基础认证
     * 2. TenantAuthenticationFilter - 在认证之后，注入租户信息
     */
    @Bean
    fun configure(http: HttpSecurity): DefaultSecurityFilterChain? =
        http.run {
            cors().and().csrf().disable()
            exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            authorizeRequests {
                // public endpoints
                it.antMatchers(HttpMethod.POST, "/public/**").permitAll()
                it.antMatchers(HttpMethod.GET, "/public/**").permitAll()
                it.antMatchers(HttpMethod.PUT, "/public/**").permitAll()
                // private endpoint
                it.anyRequest().authenticated()
            }
            authenticationProvider(createAuthenticationProvider())
            
            // 添加 JWT 过滤器
            addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            
            // 添加 Tenant 过滤器（在 JWT 过滤器之后）
            addFilterAfter(tenantAuthenticationFilter, JwtRequestFilter::class.java)
        }.build()


    @Bean
    fun createAuthenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    @Bean
    fun authenticationManager(authConfiguration: AuthenticationConfiguration) = 
        authConfiguration.authenticationManager
}

