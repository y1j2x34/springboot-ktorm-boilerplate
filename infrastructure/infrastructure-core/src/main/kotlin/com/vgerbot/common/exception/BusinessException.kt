package com.vgerbot.common.exception

/**
 * 业务异常基类
 * 所有业务异常都应继承此类
 * 
 * 支持两种错误码方式：
 * 1. 使用ErrorCode枚举（推荐）
 * 2. 使用HTTP状态码（向后兼容）
 */
open class BusinessException(
    message: String,
    val code: Int = 400,
    val errorCode: ErrorCode? = null,
    val details: Map<String, Any>? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    /**
     * 使用ErrorCode枚举构造异常（推荐方式）
     * 
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息（可选，如果为null则使用errorCode的默认消息）
     * @param details 额外的错误详情
     * @param cause 原始异常
     */
    constructor(
        errorCode: ErrorCode,
        message: String? = null,
        details: Map<String, Any>? = null,
        cause: Throwable? = null
    ) : this(
        message = message ?: errorCode.message,
        code = errorCode.httpStatus.value(),
        errorCode = errorCode,
        details = details,
        cause = cause
    )
    
    /**
     * 获取业务错误码（优先返回ErrorCode的code，否则返回HTTP状态码）
     */
    val businessCode: Int
        get() = errorCode?.code ?: code
}

/**
 * 资源未找到异常
 */
class NotFoundException(
    message: String = "资源不存在",
    errorCode: ErrorCode = CommonErrorCode.COMMON_RESOURCE_NOT_FOUND,
    details: Map<String, Any>? = null
) : BusinessException(message, errorCode.httpStatus.value(), errorCode, details)

/**
 * 资源冲突异常（如重复创建）
 */
class ConflictException(
    message: String = "资源冲突",
    errorCode: ErrorCode = CommonErrorCode.COMMON_RESOURCE_CONFLICT,
    details: Map<String, Any>? = null
) : BusinessException(message, errorCode.httpStatus.value(), errorCode, details)

/**
 * 未授权异常
 */
class UnauthorizedException(
    message: String = "未授权",
    errorCode: ErrorCode = CommonErrorCode.COMMON_UNAUTHORIZED,
    details: Map<String, Any>? = null
) : BusinessException(message, errorCode.httpStatus.value(), errorCode, details)

/**
 * 禁止访问异常
 */
class ForbiddenException(
    message: String = "权限不足",
    errorCode: ErrorCode = CommonErrorCode.COMMON_FORBIDDEN,
    details: Map<String, Any>? = null
) : BusinessException(message, errorCode.httpStatus.value(), errorCode, details)

/**
 * 参数验证异常
 */
class ValidationException(
    message: String = "参数验证失败",
    val field: String? = null,
    errorCode: ErrorCode = CommonErrorCode.COMMON_PARAM_INVALID,
    details: Map<String, Any>? = null
) : BusinessException(message, errorCode.httpStatus.value(), errorCode, details) {
    constructor(message: String, field: String) : this(
        message, 
        field, 
        CommonErrorCode.COMMON_PARAM_INVALID, 
        mapOf("field" to field)
    )
}

