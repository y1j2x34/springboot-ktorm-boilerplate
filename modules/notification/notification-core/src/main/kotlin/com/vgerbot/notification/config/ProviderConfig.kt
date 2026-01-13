package com.vgerbot.notification.config

import com.vgerbot.notification.NotificationType
import com.vgerbot.notification.ProviderType

/**
 * 提供商配置
 * 
 * 用于存储和传递提供商的配置信息
 * 配置信息以 Map 形式存储，便于扩展
 */
data class ProviderConfig(
    /**
     * 提供商类型
     */
    val providerType: ProviderType,
    
    /**
     * 是否启用
     */
    val enabled: Boolean = true,
    
    /**
     * 支持的通知类型
     */
    val supportedTypes: Set<NotificationType>,
    
    /**
     * 配置参数（JSON格式或Map）
     * 例如：
     * - SMTP: host, port, username, password, from等
     * - 阿里云SMS: accessKeyId, accessKeySecret, signName等
     * - 腾讯云SMS: secretId, secretKey, appId等
     */
    val config: Map<String, Any>
) {
    /**
     * 获取配置值
     */
    fun getConfigValue(key: String): Any? = config[key]
    
    /**
     * 获取配置值（字符串）
     */
    fun getConfigString(key: String): String? = config[key]?.toString()
    
    /**
     * 获取配置值（整数）
     */
    fun getConfigInt(key: String): Int? = config[key]?.let {
        when (it) {
            is Int -> it
            is Number -> it.toInt()
            is String -> it.toIntOrNull()
            else -> null
        }
    }
    
    /**
     * 获取配置值（布尔值）
     */
    fun getConfigBoolean(key: String): Boolean? = config[key]?.let {
        when (it) {
            is Boolean -> it
            is String -> it.toBooleanStrictOrNull()
            else -> null
        }
    }
}

