package com.vgerbot.notification.dto

import com.vgerbot.notification.NotificationStatus
import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType

/**
 * 通知记录DTO
 * 
 * 用于查询和展示通知发送记录
 */
data class NotificationRecordDto(
    /**
     * 记录ID
     */
    val id: Long? = null,
    
    /**
     * 通知类型
     */
    val type: NotificationType,
    
    /**
     * 接收者
     */
    val recipient: String,
    
    /**
     * 通知标题
     */
    val subject: String? = null,
    
    /**
     * 通知内容
     */
    val content: String,
    
    /**
     * 使用的提供商类型
     */
    val providerType: ProviderType? = null,
    
    /**
     * 发送状态
     */
    val status: NotificationStatus,
    
    /**
     * 提供商返回的消息ID
     */
    val providerMessageId: String? = null,
    
    /**
     * 错误消息
     */
    val errorMessage: String? = null,
    
    /**
     * 发送时间
     */
    val sentAt: Long? = null,
    
    /**
     * 创建时间
     */
    val createdAt: Long? = null
)

