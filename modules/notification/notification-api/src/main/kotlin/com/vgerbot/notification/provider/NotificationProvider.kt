package com.vgerbot.notification.provider

import com.vgerbot.notification.NotificationResult
import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType

/**
 * 通知提供商接口
 * 
 * 所有通知提供商必须实现此接口
 * 实现类应该通过 Spring 的 @Component 注解注册为 Bean
 */
interface NotificationProvider {
    /**
     * 获取提供商类型
     */
    fun getProviderType(): ProviderType
    
    /**
     * 获取支持的通知类型
     */
    fun getSupportedNotificationTypes(): Set<NotificationType>
    
    /**
     * 检查提供商是否已配置并可用
     */
    fun isAvailable(): Boolean
    
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
    fun sendBatch(requests: List<NotificationRequest>): List<NotificationResult> {
        return requests.map { send(it) }
    }
}

