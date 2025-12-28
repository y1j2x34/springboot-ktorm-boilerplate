package com.vgerbot.tenant.configuration

import com.vgerbot.tenant.filter.TenantAuthenticationFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * 租户过滤器注册配置
 * 
 * 通过 Spring Boot 的 FilterRegistrationBean 将租户过滤器注册到 Servlet 容器中
 * 这种方式不需要修改现有的 Security 配置
 */
@Configuration
class TenantFilterRegistration {
    
    private val logger = LoggerFactory.getLogger(TenantFilterRegistration::class.java)
    
    @Autowired
    private lateinit var tenantAuthenticationFilter: TenantAuthenticationFilter
    
    /**
     * 注册租户过滤器
     * 
     * 注意：这个方法会将过滤器注册为普通的 Servlet Filter
     * 但我们实际上需要它在 Spring Security 过滤器链中
     * 
     * 因此，推荐的集成方式仍然是在 Security 配置中使用 TenantSecurityConfigurer
     */
    @Bean
    fun tenantFilterRegistrationBean(): FilterRegistrationBean<TenantAuthenticationFilter> {
        val registrationBean = FilterRegistrationBean<TenantAuthenticationFilter>()
        registrationBean.filter = tenantAuthenticationFilter
        registrationBean.order = Ordered.LOWEST_PRECEDENCE - 1
        registrationBean.isEnabled = false // 禁用自动注册，因为我们要通过 Security 配置注册
        
        logger.info("Tenant authentication filter registration bean created (disabled for auto-registration)")
        return registrationBean
    }
}

