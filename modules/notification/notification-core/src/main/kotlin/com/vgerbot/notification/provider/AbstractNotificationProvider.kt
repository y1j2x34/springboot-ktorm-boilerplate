package com.vgerbot.notification.provider

import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType
import org.slf4j.LoggerFactory

/**
 * 抽象通知提供商基类
 * 
 * 提供通用的实现，子类只需实现具体的发送逻辑
 */
abstract class AbstractNotificationProvider(
    private val providerTypeValue: ProviderType,
    private val supportedTypesValue: Set<NotificationType>
) : NotificationProvider {
    
    protected val logger = LoggerFactory.getLogger(javaClass)
    
    override fun getProviderType(): ProviderType = providerTypeValue
    
    override fun getSupportedNotificationTypes(): Set<NotificationType> = supportedTypesValue
    
    override fun isAvailable(): Boolean {
        return try {
            checkAvailability()
        } catch (e: Exception) {
            logger.warn("检查提供商 {} 可用性时出错", providerTypeValue, e)
            false
        }
    }
    
    /**
     * 检查提供商是否可用
     * 子类可以重写此方法来实现自定义的可用性检查
     */
    protected open fun checkAvailability(): Boolean {
        // 默认实现：检查配置是否完整
        // 子类可以重写此方法
        return true
    }
    
    /**
     * 执行实际的发送逻辑
     * 子类必须实现此方法
     */
    protected abstract fun doSend(request: NotificationRequest): com.vgerbot.notification.NotificationResult
    
    override fun send(request: NotificationRequest): com.vgerbot.notification.NotificationResult {
        // 验证请求类型是否支持
        if (!supportedTypesValue.contains(request.type)) {
            return com.vgerbot.notification.NotificationResult.failure(
                message = "提供商 ${providerTypeValue} 不支持通知类型 ${request.type}",
                errorCode = "UNSUPPORTED_TYPE"
            )
        }
        
        // 检查是否可用
        if (!isAvailable()) {
            return com.vgerbot.notification.NotificationResult.failure(
                message = "提供商 ${providerTypeValue} 当前不可用",
                errorCode = "PROVIDER_UNAVAILABLE"
            )
        }
        
        // 执行发送
        return try {
            doSend(request)
        } catch (e: Exception) {
            logger.error("提供商 {} 发送通知时出错", providerTypeValue, e)
            com.vgerbot.notification.NotificationResult.failure(
                message = "发送失败: ${e.message}",
                errorCode = "SEND_ERROR",
                errorDetails = mapOf("exception" to e.javaClass.simpleName)
            )
        }
    }
}

