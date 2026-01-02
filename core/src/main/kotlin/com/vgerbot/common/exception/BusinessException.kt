package com.vgerbot.common.exception

/**
 * 业务异常基类
 * 所有业务异常都应继承此类
 */
open class BusinessException(
    message: String,
    val code: Int = 400,
    val details: Map<String, Any>? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 资源未找到异常
 */
class NotFoundException(
    message: String = "资源不存在",
    details: Map<String, Any>? = null
) : BusinessException(message, 404, details)

/**
 * 资源冲突异常（如重复创建）
 */
class ConflictException(
    message: String = "资源冲突",
    details: Map<String, Any>? = null
) : BusinessException(message, 409, details)

/**
 * 未授权异常
 */
class UnauthorizedException(
    message: String = "未授权",
    details: Map<String, Any>? = null
) : BusinessException(message, 401, details)

/**
 * 禁止访问异常
 */
class ForbiddenException(
    message: String = "权限不足",
    details: Map<String, Any>? = null
) : BusinessException(message, 403, details)

/**
 * 参数验证异常
 */
class ValidationException(
    message: String = "参数验证失败",
    val field: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(message, 400, details) {
    constructor(message: String, field: String) : this(message, field, mapOf("field" to field))
}

