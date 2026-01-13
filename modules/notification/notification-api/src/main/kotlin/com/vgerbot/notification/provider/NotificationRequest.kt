package com.vgerbot.notification.provider

import com.vgerbot.notification.NotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 通知请求
 * 
 * 通用的通知请求对象，包含发送通知所需的基本信息
 */
data class NotificationRequest(
    /**
     * 通知类型
     */
    @field:NotNull(message = "通知类型不能为空")
    val type: NotificationType,
    
    /**
     * 接收者（邮箱地址、手机号等）
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
     * 模板ID（如果使用模板）
     */
    val templateId: String? = null,
    
    /**
     * 模板参数（JSON格式或Map）
     */
    val templateParams: Map<String, Any>? = null,
    
    /**
     * 扩展参数（提供商特定的配置）
     */
    val extraParams: Map<String, Any>? = null,
    
    /**
     * 优先级（数字，越大优先级越高）
     */
    val priority: Int = 0,
    
    /**
     * 延迟发送时间（Unix时间戳，毫秒）
     */
    val scheduledTime: Long? = null
)

