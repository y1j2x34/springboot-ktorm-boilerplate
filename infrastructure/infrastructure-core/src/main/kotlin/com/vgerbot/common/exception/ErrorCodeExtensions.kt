package com.vgerbot.common.exception

import org.springframework.http.HttpStatus

/**
 * 错误码统一访问入口
 * 
 * 使用方式：
 * ```kotlin
 * // 方式1：直接导入枚举类使用（推荐）
 * import com.vgerbot.common.exception.CommonErrorCode
 * import com.vgerbot.auth.exception.AuthErrorCode
 * throw CommonErrorCode.COMMON_PARAM_INVALID.exception()
 * throw AuthErrorCode.AUTH_USER_NOT_FOUND.exception()
 * 
 * // 方式2：通过ErrorCodeAccessor对象查找
 * ErrorCodeAccessor.findByCode(200200)?.exception() // 返回对应的异常
 * ```
 */
object ErrorCodeAccessor {
    /**
     * 根据错误码查找对应的ErrorCode
     */
    fun findByCode(code: Int): ErrorCode? {
        return ErrorCodes.findByCode(code)
    }
}

/**
 * 类型别名，提供更简洁的访问方式
 */
typealias EC = ErrorCodeAccessor

/**
 * ErrorCode 扩展函数：根据错误码自动创建对应的异常
 * 
 * 根据 HTTP 状态码自动选择合适的异常类型：
 * - 404 (NOT_FOUND) -> NotFoundException
 * - 409 (CONFLICT) -> ConflictException
 * - 401 (UNAUTHORIZED) -> UnauthorizedException
 * - 403 (FORBIDDEN) -> ForbiddenException
 * - 400 (BAD_REQUEST) -> BusinessException 或 ValidationException
 * - 其他 -> BusinessException
 * 
 * 使用示例：
 * ```kotlin
 * // 使用默认消息
 * throw AuthErrorCode.AUTH_USER_NOT_FOUND.exception()
 * 
 * // 使用自定义消息
 * throw AuthErrorCode.AUTH_INVALID_CREDENTIALS.exception("登录失败，请重试")
 * 
 * // 带详情信息
 * throw CommonErrorCode.COMMON_PARAM_INVALID.exception(
 *     details = mapOf("field" to "email")
 * )
 * ```
 * 
 * @param message 自定义错误消息（可选，如果为null则使用errorCode的默认消息）
 * @param details 额外的错误详情（可选）
 * @param cause 原始异常（可选）
 * @return 对应的业务异常
 */
fun ErrorCode.exception(
    message: String? = null,
    details: Map<String, Any>? = null,
    cause: Throwable? = null
): BusinessException {
    val finalMessage = message ?: this.message
    
    return when (httpStatus) {
        HttpStatus.NOT_FOUND -> NotFoundException(
            message = finalMessage,
            errorCode = this,
            details = details
        )
        HttpStatus.CONFLICT -> ConflictException(
            message = finalMessage,
            errorCode = this,
            details = details
        )
        HttpStatus.UNAUTHORIZED -> UnauthorizedException(
            message = finalMessage,
            errorCode = this,
            details = details
        )
        HttpStatus.FORBIDDEN -> ForbiddenException(
            message = finalMessage,
            errorCode = this,
            details = details
        )
        HttpStatus.BAD_REQUEST -> {
            // 对于参数验证相关的错误码，使用 ValidationException
            if (code % 10000 / 100 == 1) { // 错误类型为 01 (参数验证错误)
                ValidationException(
                    message = finalMessage,
                    field = null,
                    errorCode = this,
                    details = details
                )
            } else {
                // 使用主构造函数（位置参数）
                BusinessException(
                    finalMessage,
                    httpStatus.value(),
                    this,
                    details,
                    cause
                )
            }
        }
        else -> {
            // 使用主构造函数（位置参数）
            BusinessException(
                finalMessage,
                httpStatus.value(),
                this,
                details,
                cause
            )
        }
    }
}

