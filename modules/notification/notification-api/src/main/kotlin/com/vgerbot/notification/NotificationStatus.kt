package com.vgerbot.notification

/**
 * 通知状态枚举
 */
enum class NotificationStatus {
    /**
     * 待发送
     */
    PENDING,
    
    /**
     * 发送中
     */
    SENDING,
    
    /**
     * 发送成功
     */
    SUCCESS,
    
    /**
     * 发送失败
     */
    FAILED,
    
    /**
     * 已取消
     */
    CANCELLED
}

