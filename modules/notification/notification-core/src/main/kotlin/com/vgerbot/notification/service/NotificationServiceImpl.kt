package com.vgerbot.notification.service

import com.vgerbot.notification.NotificationResult
import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.config.NotificationProperties
import com.vgerbot.notification.exception.NotificationErrorCode
import com.vgerbot.notification.provider.NotificationProvider
import com.vgerbot.notification.provider.NotificationRequest
import com.vgerbot.notification.provider.ProviderManager
import com.vgerbot.common.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 通知服务实现
 */
@Service
class NotificationServiceImpl(
    private val providerManager: ProviderManager,
    private val properties: NotificationProperties
) : NotificationService {
    
    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    
    override fun send(request: NotificationRequest): NotificationResult {
        if (!properties.enabled) {
            logger.warn("通知模块已禁用")
            throw BusinessException(NotificationErrorCode.NOTIFICATION_ERROR, "通知模块已禁用")
        }
        
        // 验证请求
        validateRequest(request)
        
        // 选择提供商
        val provider = providerManager.selectProvider(request.type)
            ?: throw BusinessException(
                NotificationErrorCode.NOTIFICATION_PROVIDER_NOT_AVAILABLE,
                "没有可用的通知提供商支持类型: ${request.type}"
            )
        
        logger.info("使用提供商 {} 发送 {} 通知到 {}", 
            provider.getProviderType(), request.type, request.recipient)
        
        try {
            // 发送通知
            val result = provider.send(request)
            
            if (result.success) {
                logger.info("通知发送成功: {}", result.providerMessageId)
            } else {
                logger.error("通知发送失败: {}", result.message)
            }
            
            return result
        } catch (e: Exception) {
            logger.error("通知发送异常", e)
            return NotificationResult.failure(
                message = "通知发送异常: ${e.message}",
                errorCode = NotificationErrorCode.NOTIFICATION_SEND_FAILED.code.toString(),
                errorDetails = mapOf("exception" to e.javaClass.simpleName)
            )
        }
    }
    
    override fun sendBatch(requests: List<NotificationRequest>): List<NotificationResult> {
        return requests.map { send(it) }
    }
    
    override fun isTypeAvailable(type: NotificationType): Boolean {
        return providerManager.isNotificationTypeAvailable(type)
    }
    
    /**
     * 验证通知请求
     */
    private fun validateRequest(request: NotificationRequest) {
        if (request.recipient.isBlank()) {
            throw BusinessException(NotificationErrorCode.NOTIFICATION_RECIPIENT_INVALID)
        }
        
        if (request.content.isBlank()) {
            throw BusinessException(NotificationErrorCode.NOTIFICATION_CONTENT_EMPTY)
        }
        
        // 验证接收者格式（根据类型）
        when (request.type) {
            NotificationType.EMAIL -> {
                if (!isValidEmail(request.recipient)) {
                    throw BusinessException(
                        NotificationErrorCode.NOTIFICATION_RECIPIENT_INVALID,
                        "邮箱地址格式无效: ${request.recipient}"
                    )
                }
            }
            NotificationType.SMS -> {
                if (!isValidPhoneNumber(request.recipient)) {
                    throw BusinessException(
                        NotificationErrorCode.NOTIFICATION_RECIPIENT_INVALID,
                        "手机号格式无效: ${request.recipient}"
                    )
                }
            }
            else -> {
                // 其他类型暂不验证格式
            }
        }
    }
    
    /**
     * 验证邮箱格式（简单验证）
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
    
    /**
     * 验证手机号格式（简单验证）
     */
    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^1[3-9]\\d{9}$"))
    }
}

