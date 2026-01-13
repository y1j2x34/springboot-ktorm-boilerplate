package com.vgerbot.asynctask.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.asynctask.entity.AsyncTasks
import com.vgerbot.asynctask.entity.TaskStatus

/**
 * 异步任务DAO接口
 */
interface AsyncTaskDao : BaseDao<AsyncTask, AsyncTasks> {
    /**
     * 查找待处理的任务（PENDING 和 RETRYING 状态，按优先级和创建时间排序）
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    fun findPendingTasks(limit: Int): List<AsyncTask>
    
    /**
     * 根据任务类型和状态查找任务
     */
    fun findByTypeAndStatus(taskType: String?, status: TaskStatus?): List<AsyncTask>
    
    /**
     * 查找超时的处理中任务（用于恢复）
     * 
     * @param maxProcessingTimeSeconds 最大处理时间（秒）
     * @return 超时的任务列表
     */
    fun findTimeoutProcessingTasks(maxProcessingTimeSeconds: Int): List<AsyncTask>
}

