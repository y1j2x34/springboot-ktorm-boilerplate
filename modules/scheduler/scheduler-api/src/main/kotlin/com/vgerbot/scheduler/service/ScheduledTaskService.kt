package com.vgerbot.scheduler.service

import com.vgerbot.scheduler.dto.CreateScheduledTaskDto
import com.vgerbot.scheduler.dto.ScheduledTaskDto
import com.vgerbot.scheduler.dto.UpdateScheduledTaskDto

/**
 * 定时任务服务接口
 */
interface ScheduledTaskService {
    /**
     * 创建定时任务
     */
    fun createTask(dto: CreateScheduledTaskDto): ScheduledTaskDto?
    
    /**
     * 更新定时任务
     */
    fun updateTask(id: Long, dto: UpdateScheduledTaskDto): Boolean
    
    /**
     * 删除定时任务
     */
    fun deleteTask(id: Long): Boolean
    
    /**
     * 根据ID获取任务
     */
    fun getTaskById(id: Long): ScheduledTaskDto?
    
    /**
     * 获取所有任务
     */
    fun getAllTasks(): List<ScheduledTaskDto>
    
    /**
     * 启用任务
     */
    fun enableTask(id: Long): Boolean
    
    /**
     * 停用任务
     */
    fun disableTask(id: Long): Boolean
    
    /**
     * 立即执行任务
     */
    fun executeTask(id: Long): Boolean
    
    /**
     * 重新加载所有任务到调度器
     */
    fun reloadTasks()
}

