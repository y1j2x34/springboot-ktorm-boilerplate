package com.vgerbot.common.dao

import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import org.ktorm.entity.Entity
import org.ktorm.schema.ColumnDeclaring
import java.time.Instant

/**
 * 支持简化审计字段的 DAO 实现基类
 * 
 * 适用于只需要记录 createdAt 和 isDeleted 的实体（如关联表）
 * 
 * 使用示例：
 * ```kotlin
 * @Repository
 * class UserRoleDaoImpl : SimpleAuditableDaoImpl<UserRole, UserRoles>(UserRoles), UserRoleDao
 * ```
 */
abstract class SimpleAuditableDaoImpl<E, T>(
    tableObject: T
) : AbstractSoftDeleteDao<E, T>(tableObject)
        where E : SimpleAuditableEntity<E>,
              T : SimpleAuditableTable<E> {
    
    override fun getIsDeletedColumn(table: T): ColumnDeclaring<Boolean> {
        return table.isDeleted
    }
    
    override fun setDeleted(entity: E, deleted: Boolean) {
        entity.isDeleted = deleted
    }
    
    // SimpleAuditableEntity 不支持 updatedAt 和 updatedBy
    override fun setUpdatedAt(entity: E, time: Instant?) {
        // 不做任何操作
    }
    
    override fun setUpdatedBy(entity: E, userId: Int?) {
        // 不做任何操作
    }
}

