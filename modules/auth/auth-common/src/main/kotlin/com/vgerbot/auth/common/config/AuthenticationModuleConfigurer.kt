package com.vgerbot.auth.common.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity

/**
 * 认证模块扩展点。
 *
 * 各认证模块可以通过实现该接口向 HttpSecurity 注入自己的配置，
 * 应用层后续只需要按顺序调用这些配置器，而不必直接依赖具体认证实现。
 */
interface AuthenticationModuleConfigurer {
    /**
     * 配置执行顺序，值越小越早执行。
     */
    val order: Int
        get() = 0

    /**
     * 向共享的 HttpSecurity 注入认证模块配置。
     */
    fun configure(http: HttpSecurity)
}
