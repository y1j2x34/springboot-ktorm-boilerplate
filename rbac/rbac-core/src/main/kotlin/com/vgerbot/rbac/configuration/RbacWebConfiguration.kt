package com.vgerbot.rbac.configuration

import com.vgerbot.rbac.interceptor.RbacInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * RBAC Web 配置
 * 
 * 注册 RBAC 拦截器到 Spring MVC
 */
@Configuration
class RbacWebConfiguration : WebMvcConfigurer {
    
    @Autowired
    lateinit var rbacInterceptor: RbacInterceptor
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rbacInterceptor)
            .addPathPatterns("/**")  // 拦截所有请求
            .excludePathPatterns(
                // 排除静态资源
                "/static/**",
                "/public/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/favicon.ico",
                // 排除健康检查端点
                "/actuator/**",
                "/health",
                // 排除 Swagger UI 和 API 文档
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                // 排除登录注册相关端点（根据实际情况调整）
                "/api/auth/**",
                "/api/captcha/**"
            )
    }
}

