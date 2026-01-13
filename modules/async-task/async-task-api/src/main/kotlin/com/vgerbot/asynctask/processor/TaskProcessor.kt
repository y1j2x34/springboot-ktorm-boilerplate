package com.vgerbot.asynctask.processor

import com.vgerbot.asynctask.config.RetryConfig
import com.vgerbot.asynctask.dto.TaskResult
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.notification.provider.NotificationRequest

/**
 * 任务处理器接口
 * 
 * 业务模块需要实现此接口来处理特定类型的异步任务
 * 
 * @param T 任务负载类型（payload的类型）
 */
interface TaskProcessor<T : Any> {
    /**
     * 获取任务类型标识
     * 此标识用于匹配任务和处理器
     */
    fun getTaskType(): String
    
    /**
     * 处理任务
     * 
     * @param task 任务实体
     * @param payload 任务负载（已反序列化的任务数据）
     * @return 任务执行结果
     */
    fun process(task: AsyncTask, payload: T): TaskResult
    
    /**
     * 获取重试配置
     * 如果返回 null，则使用提交任务时指定的重试配置，如果没有指定则不重试
     * 
     * @return 重试配置，或 null 表示不重试
     */
    fun getRetryConfig(): RetryConfig? = null
    
    /**
     * 获取任务优先级
     * 此方法允许根据任务负载动态计算优先级
     * 如果返回 null，则使用提交任务时指定的优先级
     * 
     * @param payload 任务负载
     * @return 优先级，或 null 表示使用默认优先级
     */
    fun getPriority(payload: T): Int? = null
    
    /**
     * 是否在成功时发送通知
     */
    fun shouldNotifyOnSuccess(): Boolean = false
    
    /**
     * 是否在失败时发送通知
     */
    fun shouldNotifyOnFailure(): Boolean = false
    
    /**
     * 构建通知请求
     * 当 shouldNotifyOnSuccess() 或 shouldNotifyOnFailure() 返回 true 时调用
     * 
     * @param task 任务实体
     * @param result 任务执行结果
     * @return 通知请求，或 null 表示不发送通知
     */
    fun buildNotificationRequest(task: AsyncTask, result: TaskResult): NotificationRequest? = null
}

