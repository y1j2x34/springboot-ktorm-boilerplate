package com.vgerbot.asynctask.config

/**
 * 重试策略枚举
 */
enum class RetryStrategy {
    /**
     * 固定间隔重试
     */
    FIXED,
    
    /**
     * 指数退避重试
     */
    EXPONENTIAL
}

