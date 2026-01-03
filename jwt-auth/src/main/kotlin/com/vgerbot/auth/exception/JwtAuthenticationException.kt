package com.vgerbot.auth.exception

import org.springframework.security.core.AuthenticationException

/**
 * JWT 认证异常基类
 */
sealed class JwtAuthenticationException(
    message: String,
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * Token 过期异常
 */
class TokenExpiredException(
    message: String = "Token has expired",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

/**
 * Token 格式无效异常
 */
class InvalidTokenException(
    message: String = "Invalid token",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

/**
 * Token 已被撤销异常
 */
class TokenRevokedException(
    message: String = "Token has been revoked",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

/**
 * Token 签名无效异常
 */
class InvalidSignatureException(
    message: String = "Invalid token signature",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

