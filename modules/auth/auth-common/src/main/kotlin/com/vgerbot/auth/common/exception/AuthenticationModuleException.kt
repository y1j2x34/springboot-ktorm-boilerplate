package com.vgerbot.auth.common.exception

import com.vgerbot.common.exception.BusinessException
import com.vgerbot.common.exception.CommonErrorCode

/**
 * 认证模块通用异常。
 */
class AuthenticationModuleException(
    message: String,
    details: Map<String, Any>? = null,
    cause: Throwable? = null
) : BusinessException(
    CommonErrorCode.COMMON_UNAUTHORIZED,
    message,
    details,
    cause
)
