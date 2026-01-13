package com.vgerbot.asynctask.config

/**
 * 重试配置
 * 
 * @param maxRetryCount 最大重试次数
 * @param strategy 重试策略
 * @param baseInterval 基础重试间隔（秒），对于指数退避策略，这是初始间隔
 */
data class RetryConfig(
    val maxRetryCount: Int,
    val strategy: RetryStrategy = RetryStrategy.FIXED,
    val baseInterval: Int = 60
) {
    init {
        require(maxRetryCount >= 0) { "最大重试次数不能为负数" }
        require(baseInterval > 0) { "重试间隔必须大于0" }
    }
    
    /**
     * 计算指定重试次数的延迟时间（秒）
     */
    fun calculateDelay(retryCount: Int): Int {
        return when (strategy) {
            RetryStrategy.FIXED -> baseInterval
            RetryStrategy.EXPONENTIAL -> {
                // 指数退避：baseInterval * 2^retryCount
                baseInterval * (1 shl retryCount).coerceAtMost(3600) // 最大1小时
            }
        }
    }
}

