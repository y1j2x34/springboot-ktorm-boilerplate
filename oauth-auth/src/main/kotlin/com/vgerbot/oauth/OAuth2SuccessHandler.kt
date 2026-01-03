package com.vgerbot.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**
 * OAuth2 认证成功处理器
 * 
 * 在 OAuth2 认证成功后，重定向到成功页面或返回 JSON 响应
 */
@Component
class OAuth2SuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    
    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)
    
    init {
        // 设置默认的成功 URL
        defaultTargetUrl = "/public/oauth2/login/success"
        // 总是使用默认 URL，忽略 saved request
        setAlwaysUseDefaultTargetUrl(true)
    }
    
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        logger.info("OAuth2 authentication successful for user: ${authentication.name}")
        super.onAuthenticationSuccess(request, response, authentication)
    }
}

