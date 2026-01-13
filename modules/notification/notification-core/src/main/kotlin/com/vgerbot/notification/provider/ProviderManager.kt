package com.vgerbot.notification.provider

import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType
import com.vgerbot.notification.config.ProviderConfig
import com.vgerbot.notification.exception.NotificationErrorCode
import com.vgerbot.common.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 提供商管理器
 * 
 * 负责管理和选择通知提供商
 */
@Component
class ProviderManager(
    private val providers: List<NotificationProvider>
) {
    private val logger = LoggerFactory.getLogger(ProviderManager::class.java)
    
    /**
     * 提供商注册表（按类型索引）
     */
    private val providerMap: Map<ProviderType, NotificationProvider> = providers
        .associateBy { it.getProviderType() }
    
    /**
     * 根据类型获取提供商
     */
    fun getProvider(type: ProviderType): NotificationProvider? {
        return providerMap[type]
    }
    
    /**
     * 根据通知类型自动选择可用的提供商
     */
    fun selectProvider(notificationType: NotificationType): NotificationProvider? {
        // 优先查找支持该通知类型且可用的提供商
        val availableProviders = providers.filter { provider ->
            provider.getSupportedNotificationTypes().contains(notificationType) &&
            provider.isAvailable()
        }
        
        if (availableProviders.isEmpty()) {
            logger.warn("没有找到支持 {} 类型且可用的通知提供商", notificationType)
            return null
        }
        
        // 返回第一个可用的提供商（未来可以实现更智能的选择策略）
        return availableProviders.first()
    }
    
    /**
     * 获取所有可用的提供商
     */
    fun getAvailableProviders(): List<NotificationProvider> {
        return providers.filter { it.isAvailable() }
    }
    
    /**
     * 检查指定类型的提供商是否可用
     */
    fun isProviderAvailable(type: ProviderType): Boolean {
        return providerMap[type]?.isAvailable() ?: false
    }
    
    /**
     * 检查指定通知类型是否可用
     */
    fun isNotificationTypeAvailable(notificationType: NotificationType): Boolean {
        return selectProvider(notificationType) != null
    }
    
    /**
     * 注册提供商配置（用于动态配置）
     * 
     * 注意：这是一个预留接口，实际实现可能需要动态创建和注册提供商Bean
     */
    fun registerProviderConfig(config: ProviderConfig) {
        logger.info("注册提供商配置: {}", config.providerType)
        // TODO: 实现动态注册提供商的逻辑
        // 这可能需要使用 ApplicationContext 来动态创建和注册 Bean
    }
}

