package com.vgerbot.authorization.config

import com.vgerbot.authorization.interceptor.AuthorizationInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Authorization 模块自动配置
 */
@Configuration
@EnableConfigurationProperties(AuthorizationProperties::class)
@ComponentScan(basePackages = ["com.vgerbot.authorization"])
@ConditionalOnProperty(prefix = "authorization", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AuthorizationAutoConfiguration(
    private val authorizationInterceptor: AuthorizationInterceptor
) : WebMvcConfigurer {
    
    /**
     * 注册授权拦截器
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authorizationInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/error",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
    }
}

