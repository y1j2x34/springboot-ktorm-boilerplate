package com.vgerbot.com.vgerbot.tenant.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct

/**
 * 租户过滤器自动配置
 * 
 * 这个配置类会在 Spring Security 配置完成后，自动将 TenantAuthenticationFilter 注册到过滤器链中
 * 
 * 注意：由于 Spring Security 的配置已经在 JwtConfiguration 中完成，
 * 我们需要通过另一种方式来集成租户过滤器。
 * 
 * 推荐的集成方式是在应用层创建一个新的配置类来覆盖 Security 配置，
 * 或者使用 Spring Boot 的自动配置特性。
 */
@Configuration
@ConditionalOnClass(name = ["org.springframework.security.config.annotation.web.builders.HttpSecurity"])
class TenantAutoConfiguration {
    
    private val logger = LoggerFactory.getLogger(TenantAutoConfiguration::class.java)
    
    @PostConstruct
    fun init() {
        logger.info("Tenant module auto-configuration initialized")
        logger.info("To integrate tenant authentication, please add TenantSecurityConfigurer to your Security configuration")
        logger.info("Example:")
        logger.info("  http.apply(tenantSecurityConfigurer)")
    }
}

