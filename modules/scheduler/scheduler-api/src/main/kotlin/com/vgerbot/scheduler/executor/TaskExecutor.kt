package com.vgerbot.scheduler.executor

/**
 * 任务执行结果
 */
data class TaskExecutionResult(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)

/**
 * 任务执行器接口
 * 所有任务执行器必须实现此接口
 */
interface TaskExecutor {
    /**
     * 执行任务
     * @param config 任务配置（JSON字符串）
     * @return 执行结果
     */
    fun execute(config: String?): TaskExecutionResult
    
    /**
     * 获取执行器类型名称
     */
    fun getExecutorType(): String
}

