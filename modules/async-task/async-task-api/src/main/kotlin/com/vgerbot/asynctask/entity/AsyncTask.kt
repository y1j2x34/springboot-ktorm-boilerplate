package com.vgerbot.asynctask.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

/**
 * 异步任务实体
 */
interface AsyncTask : AuditableEntity<AsyncTask> {
    companion object : Entity.Factory<AsyncTask>()
    
    val id: Long
    var taskType: String
    var taskStatus: String  // TaskStatus 的字符串值
    var priority: Int
    var payload: String  // JSON格式的任务数据
    var result: String?  // JSON格式的执行结果
    var errorMessage: String?
    var retryCount: Int
    var maxRetryCount: Int
    var retryStrategy: String?  // RetryStrategy 的字符串值
    var retryInterval: Int?
    var processorClass: String?
    var startedAt: java.time.Instant?
    var completedAt: java.time.Instant?
    var tenantId: Int?
    var statusFlag: Int  // 状态标志：1-正常，0-已取消
}

/**
 * 异步任务表定义
 */
object AsyncTasks : AuditableTable<AsyncTask>("async_task") {
    val id = long("id").primaryKey().bindTo { it.id }
    val taskType = varchar("task_type").bindTo { it.taskType }
    val taskStatus = varchar("status").bindTo { it.taskStatus }
    val priority = int("priority").bindTo { it.priority }
    val payload = text("payload").bindTo { it.payload }
    val result = text("result").bindTo { it.result }
    val errorMessage = text("error_message").bindTo { it.errorMessage }
    val retryCount = int("retry_count").bindTo { it.retryCount }
    val maxRetryCount = int("max_retry_count").bindTo { it.maxRetryCount }
    val retryStrategy = varchar("retry_strategy").bindTo { it.retryStrategy }
    val retryInterval = int("retry_interval").bindTo { it.retryInterval }
    val processorClass = varchar("processor_class").bindTo { it.processorClass }
    val startedAt = timestamp("started_at").bindTo { it.startedAt }
    val completedAt = timestamp("completed_at").bindTo { it.completedAt }
    val tenantId = int("tenant_id").bindTo { it.tenantId }
    val statusFlag = int("status_flag").bindTo { it.statusFlag }
}

val Database.asyncTasks get() = this.sequenceOf(AsyncTasks)

