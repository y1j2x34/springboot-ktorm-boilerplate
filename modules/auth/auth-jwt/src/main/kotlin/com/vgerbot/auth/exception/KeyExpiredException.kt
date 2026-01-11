package com.vgerbot.auth.exception

/**
 * 密钥过期异常
 */
class KeyExpiredException(
    message: String = "密钥已过期",
    cause: Throwable? = null
) : JwtAuthenticationException(message, cause)

