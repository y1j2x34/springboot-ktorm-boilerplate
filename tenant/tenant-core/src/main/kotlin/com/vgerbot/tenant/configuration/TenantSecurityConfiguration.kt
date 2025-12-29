package com.vgerbot.com.vgerbot.tenant.configuration

import com.vgerbot.com.vgerbot.tenant.filter.TenantAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 租户模块配置
 * 
 * 将 TenantAuthenticationFilter 注册到 Spring Security 过滤器链中
 * 该过滤器会在 UsernamePasswordAuthenticationFilter 之后执行
 */
@Configuration
@ConditionalOnClass(name = ["org.springframework.security.config.annotation.web.builders.HttpSecurity"])
class TenantSecurityConfiguration {
    
    @Autowired
    private lateinit var tenantAuthenticationFilter: TenantAuthenticationFilter
    
    /**
     * 创建租户安全配置适配器
     * 
     * 这个 Bean 可以被应用的 Security 配置类使用，通过 apply() 方法添加到过滤器链中
     */
    @Bean
    fun tenantSecurityConfigurer(): TenantSecurityConfigurer {
        return TenantSecurityConfigurer(tenantAuthenticationFilter)
    }
}

/**
 * 租户安全配置适配器
 * 
 * 使用方式：
 * ```kotlin
 * @Configuration
 * class SecurityConfig {
 *     @Autowired
 *     private lateinit var tenantSecurityConfigurer: TenantSecurityConfigurer
 *     
 *     fun configure(http: HttpSecurity) {
 *         http.apply(tenantSecurityConfigurer)
 *             // ... other configurations
 *     }
 * }
 * ```
 */
class TenantSecurityConfigurer(
    private val tenantAuthenticationFilter: TenantAuthenticationFilter
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
    
    override fun configure(http: HttpSecurity) {
        // 将租户过滤器添加到 UsernamePasswordAuthenticationFilter 之后
        // 这样可以确保在 JWT 认证完成后再注入租户信息
        http.addFilterAfter(tenantAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}

