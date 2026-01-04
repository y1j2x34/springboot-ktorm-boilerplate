package com.vgerbot.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

/**
 * OAuth2 认证失败处理器
 * 
 * 在 OAuth2 认证失败后，重定向到失败页面或返回错误响应
 */
@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    
    private val logger = LoggerFactory.getLogger(OAuth2FailureHandler::class.java)
    
    init {
        // 设置默认的失败 URL
        super.setDefaultFailureUrl("/public/oauth2/login/failure")
    }
    
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.warn("OAuth2 authentication failed: ${exception.message}", exception)
        super.onAuthenticationFailure(request, response, exception)
    }
}

