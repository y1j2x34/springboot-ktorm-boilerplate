package com.vgerbot.scheduler.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.scheduler.entity.ScheduledTask
import com.vgerbot.scheduler.entity.ScheduledTasks

/**
 * 定时任务DAO接口
 */
interface ScheduledTaskDao : BaseDao<ScheduledTask, ScheduledTasks> {
    /**
     * 根据任务名称查找任务
     */
    fun findByTaskName(taskName: String): ScheduledTask?
    
    /**
     * 查找所有启用的任务
     */
    fun findAllEnabled(): List<ScheduledTask>
    
    /**
     * 根据任务类型查找任务
     */
    fun findByTaskType(taskType: String): List<ScheduledTask>
}

