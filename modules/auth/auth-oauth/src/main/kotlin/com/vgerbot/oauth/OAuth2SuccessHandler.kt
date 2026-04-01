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
 * 在 OAuth2 认证成功后，始终重定向到配置的成功页面
 */
@Component
class OAuth2SuccessHandler(
    oauth2Properties: OAuth2Properties
) : SimpleUrlAuthenticationSuccessHandler() {
    
    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)
    
    init {
        defaultTargetUrl = oauth2Properties.successRedirectUri
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

