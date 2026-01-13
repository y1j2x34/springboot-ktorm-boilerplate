package com.vgerbot.notification.dto

import com.vgerbot.notification.NotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 发送通知请求DTO
 */
data class SendNotificationDto(
    /**
     * 通知类型
     */
    @field:NotNull(message = "通知类型不能为空")
    val type: NotificationType,
    
    /**
     * 接收者
     */
    @field:NotBlank(message = "接收者不能为空")
    val recipient: String,
    
    /**
     * 通知标题
     */
    val subject: String? = null,
    
    /**
     * 通知内容
     */
    @field:NotBlank(message = "通知内容不能为空")
    val content: String,
    
    /**
     * 模板ID
     */
    val templateId: String? = null,
    
    /**
     * 模板参数（JSON字符串）
     */
    val templateParams: String? = null,
    
    /**
     * 扩展参数（JSON字符串）
     */
    val extraParams: String? = null,
    
    /**
     * 优先级
     */
    val priority: Int = 0,
    
    /**
     * 延迟发送时间（Unix时间戳，毫秒）
     */
    val scheduledTime: Long? = null
)

