package com.vgerbot.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * JWT 认证入口点
 * 
 * 处理未认证请求，返回标准的 401 错误响应
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        logger.debug("Authentication failed for request: ${request.requestURI}, error: ${authException?.message}")
        
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val errorResponse = mapOf(
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "error" to "Unauthorized",
            "message" to (authException?.message ?: "Authentication required"),
            "path" to request.requestURI,
            "timestamp" to System.currentTimeMillis()
        )
        
        objectMapper.writeValue(response.outputStream, errorResponse)
    }
}
