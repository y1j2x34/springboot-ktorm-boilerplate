package com.vgerbot.notification.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 通知模块错误码
 * 模块代码：95
 */
enum class NotificationErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 通知通用错误 (9500XX) ====================
    NOTIFICATION_ERROR(950000, "通知错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 通知参数验证错误 (9501XX) ====================
    NOTIFICATION_PARAM_INVALID(950100, "通知参数验证失败", HttpStatus.BAD_REQUEST),
    NOTIFICATION_RECIPIENT_INVALID(950101, "接收者格式无效", HttpStatus.BAD_REQUEST),
    NOTIFICATION_CONTENT_EMPTY(950102, "通知内容不能为空", HttpStatus.BAD_REQUEST),
    
    // ==================== 通知资源不存在 (9502XX) ====================
    NOTIFICATION_RECORD_NOT_FOUND(950200, "通知记录不存在", HttpStatus.NOT_FOUND),
    NOTIFICATION_PROVIDER_NOT_FOUND(950201, "通知提供商不存在", HttpStatus.NOT_FOUND),
    NOTIFICATION_TEMPLATE_NOT_FOUND(950202, "通知模板不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 通知资源冲突 (9503XX) ====================
    NOTIFICATION_PROVIDER_EXISTS(950300, "通知提供商已存在", HttpStatus.CONFLICT),
    
    // ==================== 通知业务逻辑错误 (9506XX) ====================
    NOTIFICATION_TYPE_NOT_SUPPORTED(950600, "不支持的通知类型", HttpStatus.BAD_REQUEST),
    NOTIFICATION_PROVIDER_NOT_AVAILABLE(950601, "通知提供商不可用", HttpStatus.SERVICE_UNAVAILABLE),
    NOTIFICATION_PROVIDER_NOT_CONFIGURED(950602, "通知提供商未配置", HttpStatus.BAD_REQUEST),
    NOTIFICATION_SEND_FAILED(950603, "通知发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ==================== 外部服务错误 (9507XX) ====================
    NOTIFICATION_PROVIDER_ERROR(950700, "通知提供商服务错误", HttpStatus.BAD_GATEWAY),
    NOTIFICATION_PROVIDER_TIMEOUT(950701, "通知提供商服务超时", HttpStatus.GATEWAY_TIMEOUT),
}

