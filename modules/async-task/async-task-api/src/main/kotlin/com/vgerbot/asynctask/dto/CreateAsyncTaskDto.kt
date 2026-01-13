package com.vgerbot.asynctask.dto

import com.vgerbot.asynctask.config.RetryConfig
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 创建异步任务DTO
 */
data class CreateAsyncTaskDto(
    /**
     * 任务类型
     */
    @field:NotBlank(message = "任务类型不能为空")
    val taskType: String,
    
    /**
     * 任务数据（将被序列化为JSON）
     */
    @field:NotNull(message = "任务数据不能为空")
    val payload: Any,
    
    /**
     * 优先级（数字越大优先级越高）
     */
    val priority: Int = 0,
    
    /**
     * 重试配置（可选）
     */
    val retryConfig: RetryConfig? = null,
    
    /**
     * 租户ID（可选）
     */
    val tenantId: Int? = null
)

