package com.vgerbot.asynctask.service

import com.vgerbot.asynctask.config.RetryConfig
import com.vgerbot.asynctask.dto.CreateAsyncTaskDto
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.asynctask.entity.TaskStatus

/**
 * 异步任务服务接口
 */
interface AsyncTaskService {
    /**
     * 提交任务
     * 
     * @param taskType 任务类型
     * @param payload 任务数据
     * @param priority 优先级（数字越大优先级越高）
     * @param retryConfig 重试配置（可选）
     * @param tenantId 租户ID（可选）
     * @return 创建的任务实体
     */
    fun <T : Any> submitTask(
        taskType: String,
        payload: T,
        priority: Int = 0,
        retryConfig: RetryConfig? = null,
        tenantId: Int? = null
    ): AsyncTask
    
    /**
     * 提交任务（使用DTO）
     * 
     * @param dto 创建任务DTO
     * @return 创建的任务实体
     */
    fun submitTask(dto: CreateAsyncTaskDto): AsyncTask
    
    /**
     * 根据ID获取任务
     * 
     * @param id 任务ID
     * @return 任务实体，如果不存在则返回 null
     */
    fun getTask(id: Long): AsyncTask?
    
    /**
     * 根据任务类型和状态查询任务列表
     * 
     * @param taskType 任务类型（可选）
     * @param status 任务状态（可选）
     * @return 任务列表
     */
    fun getTasksByType(taskType: String? = null, status: TaskStatus? = null): List<AsyncTask>
    
    /**
     * 取消任务
     * 只能取消 PENDING 或 RETRYING 状态的任务
     * 
     * @param id 任务ID
     * @return 是否成功取消
     */
    fun cancelTask(id: Long): Boolean
    
    /**
     * 手动重试任务
     * 只能重试 FAILURE 状态的任务
     * 
     * @param id 任务ID
     * @return 是否成功触发重试
     */
    fun retryTask(id: Long): Boolean
}

