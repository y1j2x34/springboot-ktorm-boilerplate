package com.vgerbot.scheduler.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

/**
 * 定时任务DTO
 */
data class ScheduledTaskDto(
    val id: Long? = null,
    val taskName: String,
    val taskType: String,  // BUILTIN 或 CLASS
    val executorType: String,
    val cronExpression: String,
    val taskConfig: String? = null,
    val description: String? = null,
    val enabled: Boolean = true,
    val lastRunTime: Instant? = null,
    val lastRunStatus: String? = null,
    val lastRunMessage: String? = null,
    val nextRunTime: Instant? = null,
    val runCount: Long = 0,
    val successCount: Long = 0,
    val failCount: Long = 0,
    val status: Boolean = true,
    val createdBy: Int? = null,
    val createdAt: Instant? = null,
    val updatedBy: Int? = null,
    val updatedAt: Instant? = null
)

/**
 * 创建定时任务DTO
 */
data class CreateScheduledTaskDto(
    @field:NotBlank(message = "任务名称不能为空")
    val taskName: String,
    
    @field:NotBlank(message = "任务类型不能为空")
    val taskType: String,  // BUILTIN 或 CLASS
    
    @field:NotBlank(message = "执行器类型不能为空")
    val executorType: String,
    
    @field:NotBlank(message = "Cron表达式不能为空")
    val cronExpression: String,
    
    val taskConfig: String? = null,
    val description: String? = null,
    val enabled: Boolean = true
)

/**
 * 更新定时任务DTO
 */
data class UpdateScheduledTaskDto(
    val taskName: String? = null,
    val taskType: String? = null,
    val executorType: String? = null,
    val cronExpression: String? = null,
    val taskConfig: String? = null,
    val description: String? = null,
    val enabled: Boolean? = null
)

/**
 * 执行任务DTO
 */
data class ExecuteTaskDto(
    @field:NotNull(message = "任务ID不能为空")
    val taskId: Long
)

