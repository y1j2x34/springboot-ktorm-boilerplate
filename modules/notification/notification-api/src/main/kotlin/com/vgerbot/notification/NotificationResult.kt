package com.vgerbot.notification

/**
 * 通知发送结果
 */
data class NotificationResult(
    /**
     * 是否发送成功
     */
    val success: Boolean,
    
    /**
     * 结果消息
     */
    val message: String? = null,
    
    /**
     * 提供商返回的消息ID（用于追踪）
     */
    val providerMessageId: String? = null,
    
    /**
     * 错误码（如果失败）
     */
    val errorCode: String? = null,
    
    /**
     * 错误详情
     */
    val errorDetails: Map<String, Any>? = null,
    
    /**
     * 发送时间（Unix时间戳，毫秒）
     */
    val sentAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 创建成功结果
         */
        fun success(
            message: String? = null,
            providerMessageId: String? = null
        ): NotificationResult {
            return NotificationResult(
                success = true,
                message = message,
                providerMessageId = providerMessageId
            )
        }
        
        /**
         * 创建失败结果
         */
        fun failure(
            message: String,
            errorCode: String? = null,
            errorDetails: Map<String, Any>? = null
        ): NotificationResult {
            return NotificationResult(
                success = false,
                message = message,
                errorCode = errorCode,
                errorDetails = errorDetails
            )
        }
    }
}

