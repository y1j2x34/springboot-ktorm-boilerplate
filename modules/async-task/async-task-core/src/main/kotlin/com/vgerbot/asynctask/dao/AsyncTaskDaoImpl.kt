package com.vgerbot.asynctask.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.asynctask.entity.AsyncTask
import com.vgerbot.asynctask.entity.AsyncTasks
import com.vgerbot.asynctask.entity.TaskStatus
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.ColumnDeclaring
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class AsyncTaskDaoImpl : AuditableDaoImpl<AsyncTask, AsyncTasks>(AsyncTasks), AsyncTaskDao {
    
    override fun findPendingTasks(limit: Int): List<AsyncTask> {
        return database.from(AsyncTasks)
            .select()
            .where {
                ((AsyncTasks.taskStatus eq TaskStatus.PENDING.name) or (AsyncTasks.taskStatus eq TaskStatus.RETRYING.name)) and
                (AsyncTasks.statusFlag eq 1)
            }
            .orderBy(AsyncTasks.priority.desc(), AsyncTasks.createdAt.asc())
            .limit(limit)
            .map { AsyncTasks.createEntity(it) }
    }
    
    override fun findByTypeAndStatus(taskType: String?, status: TaskStatus?): List<AsyncTask> {
        val conditions = mutableListOf<ColumnDeclaring<Boolean>>()
        conditions.add(AsyncTasks.statusFlag eq 1)
        
        taskType?.let {
            conditions.add(AsyncTasks.taskType eq it)
        }
        
        status?.let {
            conditions.add(AsyncTasks.taskStatus eq it.name)
        }
        
        return findList { conditions.reduce { acc, cond -> acc and cond } }
    }
    
    override fun findTimeoutProcessingTasks(maxProcessingTimeSeconds: Int): List<AsyncTask> {
        val timeoutThreshold = Instant.now().minusSeconds(maxProcessingTimeSeconds.toLong())
        return database.from(AsyncTasks)
            .select()
            .where {
                (AsyncTasks.taskStatus eq TaskStatus.PROCESSING.name) and
                (AsyncTasks.statusFlag eq 1) and
                (AsyncTasks.startedAt lessEq timeoutThreshold)
            }
            .map { AsyncTasks.createEntity(it) }
    }
}

