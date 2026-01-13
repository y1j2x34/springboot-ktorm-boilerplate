package com.vgerbot.notification

/**
 * 通知提供商类型枚举
 * 
 * 定义系统支持的通知提供商类型
 * 未来可以扩展支持更多提供商
 */
enum class ProviderType {
    /**
     * SMTP 邮件服务器
     */
    SMTP,
    
    /**
     * 阿里云短信服务
     */
    ALIYUN_SMS,
    
    /**
     * 腾讯云短信服务
     */
    TENCENT_SMS,
    
    /**
     * 华为云短信服务
     */
    HUAWEI_SMS,
    
    /**
     * 自定义提供商（可扩展）
     */
    CUSTOM
}

