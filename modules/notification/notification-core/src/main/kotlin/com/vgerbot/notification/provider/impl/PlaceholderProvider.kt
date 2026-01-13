package com.vgerbot.notification.provider.impl

import com.vgerbot.notification.NotificationResult
import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType
import com.vgerbot.notification.provider.AbstractNotificationProvider
import com.vgerbot.notification.provider.NotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 占位符提供商实现
 * 
 * 这是一个示例实现，用于演示如何实现通知提供商
 * 实际使用时，开发者应该实现具体的提供商（如SMTP、阿里云SMS等）
 * 
 * 此提供商默认禁用，仅作为示例
 */
@Component
@ConditionalOnProperty(
    prefix = "notification.provider.placeholder",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class PlaceholderProvider : AbstractNotificationProvider(
    providerTypeValue = ProviderType.CUSTOM,
    supportedTypesValue = setOf(NotificationType.EMAIL, NotificationType.SMS)
) {
    
    override fun checkAvailability(): Boolean {
        // 占位符提供商始终返回false，表示不可用
        // 实际提供商应该检查配置是否完整
        return false
    }
    
    override fun doSend(request: NotificationRequest): NotificationResult {
        logger.warn("占位符提供商被调用，这是一个示例实现")
        
        // 模拟发送逻辑
        return NotificationResult.success(
            message = "占位符提供商：通知已记录（未实际发送）",
            providerMessageId = "placeholder-${System.currentTimeMillis()}"
        )
    }
}

