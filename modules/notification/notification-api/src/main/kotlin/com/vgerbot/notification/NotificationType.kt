package com.vgerbot.notification

/**
 * 通知类型枚举
 * 
 * 定义系统支持的通知类型
 */
enum class NotificationType {
    /**
     * 邮件通知
     */
    EMAIL,
    
    /**
     * 短信通知
     */
    SMS,
    
    /**
     * 站内消息
     */
    IN_APP,
    
    /**
     * 推送通知
     */
    PUSH,
    
    /**
     * 微信消息
     */
    WECHAT,
    
    /**
     * 其他类型（可扩展）
     */
    OTHER
}

