package com.vgerbot.asynctask.entity

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    /**
     * 待处理
     */
    PENDING,
    
    /**
     * 处理中
     */
    PROCESSING,
    
    /**
     * 成功
     */
    SUCCESS,
    
    /**
     * 失败
     */
    FAILURE,
    
    /**
     * 重试中
     */
    RETRYING
}

