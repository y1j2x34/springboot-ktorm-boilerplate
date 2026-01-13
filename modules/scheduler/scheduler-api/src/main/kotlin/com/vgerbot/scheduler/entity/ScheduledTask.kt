package com.vgerbot.scheduler.entity

import com.vgerbot.common.entity.StatusAuditableEntity
import com.vgerbot.common.entity.StatusAuditableTable
import com.vgerbot.scheduler.dto.ScheduledTaskDto
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

/**
 * 定时任务实体
 */
interface ScheduledTask : StatusAuditableEntity<ScheduledTask> {
    companion object : Entity.Factory<ScheduledTask>()
    
    val id: Long
    var taskName: String
    var taskType: String  // BUILTIN 或 CLASS
    var executorType: String  // 内置类型名称或Java类全限定名
    var cronExpression: String
    var taskConfig: String?  // JSON格式的配置
    var description: String?
    var enabled: Boolean
    var lastRunTime: java.time.Instant?
    var lastRunStatus: String?  // SUCCESS, FAILED, RUNNING
    var lastRunMessage: String?
    var nextRunTime: java.time.Instant?
    var runCount: Long
    var successCount: Long
    var failCount: Long
}

/**
 * 定时任务表定义
 */
object ScheduledTasks : StatusAuditableTable<ScheduledTask>("scheduled_task") {
    val id = long("id").primaryKey().bindTo { it.id }
    val taskName = varchar("task_name").bindTo { it.taskName }
    val taskType = varchar("task_type").bindTo { it.taskType }
    val executorType = varchar("executor_type").bindTo { it.executorType }
    val cronExpression = varchar("cron_expression").bindTo { it.cronExpression }
    val taskConfig = text("task_config").bindTo { it.taskConfig }
    val description = varchar("description").bindTo { it.description }
    val enabled = boolean("enabled").bindTo { it.enabled }
    val lastRunTime = timestamp("last_run_time").bindTo { it.lastRunTime }
    val lastRunStatus = varchar("last_run_status").bindTo { it.lastRunStatus }
    val lastRunMessage = text("last_run_message").bindTo { it.lastRunMessage }
    val nextRunTime = timestamp("next_run_time").bindTo { it.nextRunTime }
    val runCount = long("run_count").bindTo { it.runCount }
    val successCount = long("success_count").bindTo { it.successCount }
    val failCount = long("fail_count").bindTo { it.failCount }
}

val Database.scheduledTasks get() = this.sequenceOf(ScheduledTasks)

/**
 * 实体转DTO扩展函数
 */
fun ScheduledTask.toDto(): ScheduledTaskDto = ScheduledTaskDto(
    id = this.id,
    taskName = this.taskName,
    taskType = this.taskType,
    executorType = this.executorType,
    cronExpression = this.cronExpression,
    taskConfig = this.taskConfig,
    description = this.description,
    enabled = this.enabled,
    lastRunTime = this.lastRunTime,
    lastRunStatus = this.lastRunStatus,
    lastRunMessage = this.lastRunMessage,
    nextRunTime = this.nextRunTime,
    runCount = this.runCount,
    successCount = this.successCount,
    failCount = this.failCount,
    status = this.status == 1,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt
)

