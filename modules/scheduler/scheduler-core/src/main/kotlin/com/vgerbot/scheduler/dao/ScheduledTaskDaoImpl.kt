package com.vgerbot.scheduler.dao

import com.vgerbot.common.dao.StatusAuditableDaoImpl
import com.vgerbot.scheduler.entity.ScheduledTask
import com.vgerbot.scheduler.entity.ScheduledTasks
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.springframework.stereotype.Repository

@Repository
class ScheduledTaskDaoImpl : StatusAuditableDaoImpl<ScheduledTask, ScheduledTasks>(ScheduledTasks), ScheduledTaskDao {
    
    override fun findByTaskName(taskName: String): ScheduledTask? {
        return findOne { (it.taskName eq taskName) and (it.status eq 1) }
    }
    
    override fun findAllEnabled(): List<ScheduledTask> {
        return findList { (it.enabled eq true) and (it.status eq 1) }
    }
    
    override fun findByTaskType(taskType: String): List<ScheduledTask> {
        return findList { (it.taskType eq taskType) and (it.status eq 1) }
    }
}

