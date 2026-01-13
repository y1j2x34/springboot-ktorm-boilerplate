package com.vgerbot.notification.service

import com.vgerbot.notification.NotificationResult
import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.provider.NotificationRequest

/**
 * 通知服务接口
 * 
 * 提供统一的通知发送接口，供其他模块使用
 */
interface NotificationService {
    /**
     * 发送通知
     * 
     * @param request 通知请求
     * @return 发送结果
     */
    fun send(request: NotificationRequest): NotificationResult
    
    /**
     * 批量发送通知
     * 
     * @param requests 通知请求列表
     * @return 发送结果列表
     */
    fun sendBatch(requests: List<NotificationRequest>): List<NotificationResult>
    
    /**
     * 发送邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param extraParams 扩展参数
     * @return 发送结果
     */
    fun sendEmail(
        to: String,
        subject: String,
        content: String,
        extraParams: Map<String, Any>? = null
    ): NotificationResult {
        return send(
            NotificationRequest(
                type = NotificationType.EMAIL,
                recipient = to,
                subject = subject,
                content = content,
                extraParams = extraParams
            )
        )
    }
    
    /**
     * 发送短信
     * 
     * @param to 收件人手机号
     * @param content 短信内容
     * @param templateId 模板ID（可选）
     * @param templateParams 模板参数（可选）
     * @param extraParams 扩展参数
     * @return 发送结果
     */
    fun sendSms(
        to: String,
        content: String,
        templateId: String? = null,
        templateParams: Map<String, Any>? = null,
        extraParams: Map<String, Any>? = null
    ): NotificationResult {
        return send(
            NotificationRequest(
                type = NotificationType.SMS,
                recipient = to,
                content = content,
                templateId = templateId,
                templateParams = templateParams,
                extraParams = extraParams
            )
        )
    }
    
    /**
     * 检查指定类型的通知是否可用
     * 
     * @param type 通知类型
     * @return 是否可用
     */
    fun isTypeAvailable(type: NotificationType): Boolean
}

