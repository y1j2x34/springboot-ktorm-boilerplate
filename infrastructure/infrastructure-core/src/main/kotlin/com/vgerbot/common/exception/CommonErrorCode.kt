package com.vgerbot.common.exception

import org.springframework.http.HttpStatus

/**
 * 通用模块错误码
 * 模块代码：10
 */
enum class CommonErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 通用错误 (1000XX) ====================
    COMMON_INTERNAL_ERROR(100000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMON_UNKNOWN_ERROR(100001, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMON_SERVICE_UNAVAILABLE(100002, "服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE),
    COMMON_TIMEOUT(100003, "请求超时", HttpStatus.REQUEST_TIMEOUT),
    
    // ==================== 参数验证错误 (1001XX) ====================
    COMMON_PARAM_INVALID(100100, "参数验证失败", HttpStatus.BAD_REQUEST),
    COMMON_PARAM_MISSING(100101, "缺少必需的参数", HttpStatus.BAD_REQUEST),
    COMMON_PARAM_TYPE_ERROR(100102, "参数类型错误", HttpStatus.BAD_REQUEST),
    COMMON_PARAM_FORMAT_ERROR(100103, "参数格式错误", HttpStatus.BAD_REQUEST),
    COMMON_REQUEST_BODY_INVALID(100104, "请求体格式错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 资源不存在 (1002XX) ====================
    COMMON_RESOURCE_NOT_FOUND(100200, "资源不存在", HttpStatus.NOT_FOUND),
    COMMON_ENDPOINT_NOT_FOUND(100201, "请求的资源不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 资源冲突 (1003XX) ====================
    COMMON_RESOURCE_CONFLICT(100300, "资源冲突", HttpStatus.CONFLICT),
    COMMON_RESOURCE_DUPLICATE(100301, "资源已存在", HttpStatus.CONFLICT),
    
    // ==================== 权限不足 (1004XX) ====================
    COMMON_FORBIDDEN(100400, "权限不足，无法访问该资源", HttpStatus.FORBIDDEN),
    COMMON_OPERATION_NOT_ALLOWED(100401, "操作不被允许", HttpStatus.FORBIDDEN),
    
    // ==================== 认证失败 (1005XX) ====================
    COMMON_UNAUTHORIZED(100500, "未授权", HttpStatus.UNAUTHORIZED),
    COMMON_TOKEN_INVALID(100501, "无效的认证令牌", HttpStatus.UNAUTHORIZED),
    COMMON_TOKEN_EXPIRED(100502, "认证令牌已过期", HttpStatus.UNAUTHORIZED),
    COMMON_TOKEN_MISSING(100503, "缺少认证令牌", HttpStatus.UNAUTHORIZED),
    
    // ==================== 业务逻辑错误 (1006XX) ====================
    COMMON_BUSINESS_ERROR(100600, "业务逻辑错误", HttpStatus.BAD_REQUEST),
    COMMON_STATE_ERROR(100601, "资源状态错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 外部服务错误 (1007XX) ====================
    COMMON_EXTERNAL_SERVICE_ERROR(100700, "外部服务调用失败", HttpStatus.BAD_GATEWAY),
    COMMON_EXTERNAL_SERVICE_TIMEOUT(100701, "外部服务调用超时", HttpStatus.GATEWAY_TIMEOUT),
    
    // ==================== 数据格式错误 (1008XX) ====================
    COMMON_DATA_FORMAT_ERROR(100800, "数据格式错误", HttpStatus.BAD_REQUEST),
    COMMON_JSON_PARSE_ERROR(100801, "JSON解析失败", HttpStatus.BAD_REQUEST),
    
    // ==================== 操作不允许 (1009XX) ====================
    COMMON_METHOD_NOT_ALLOWED(100900, "请求方法不允许", HttpStatus.METHOD_NOT_ALLOWED),
    COMMON_MEDIA_TYPE_NOT_SUPPORTED(100901, "不支持的媒体类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    COMMON_FILE_TOO_LARGE(100902, "上传文件大小超过限制", HttpStatus.PAYLOAD_TOO_LARGE),
}

