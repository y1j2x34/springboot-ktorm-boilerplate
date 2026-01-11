package com.vgerbot.auth.exception

/**
 * 密钥不存在异常
 */
class KeyNotFoundException(
    message: String = "密钥不存在",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

