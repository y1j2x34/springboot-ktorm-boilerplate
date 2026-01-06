package com.vgerbot.common.dao

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import org.ktorm.entity.Entity
import org.ktorm.schema.ColumnDeclaring
import java.time.Instant

/**
 * 支持完整审计字段的 DAO 实现基类
 * 
 * 适用于需要记录 createdAt, updatedAt, createdBy, updatedBy 和 isDeleted 的实体
 * 
 * 使用示例：
 * ```kotlin
 * @Repository
 * class RoleDaoImpl : AuditableDaoImpl<Role, Roles>(Roles), RoleDao
 * ```
 */
abstract class AuditableDaoImpl<E, T>(
    tableObject: T
) : AbstractSoftDeleteDao<E, T>(tableObject)
        where E : AuditableEntity<E>,
              T : AuditableTable<E> {
    
    override fun getIsDeletedColumn(table: T): ColumnDeclaring<Boolean> {
        return table.isDeleted
    }
    
    override fun setDeleted(entity: E, deleted: Boolean) {
        entity.isDeleted = deleted
    }
    
    override fun setUpdatedAt(entity: E, time: Instant?) {
        entity.updatedAt = time
    }
    
    override fun setUpdatedBy(entity: E, userId: Int?) {
        entity.updatedBy = userId
    }
}
