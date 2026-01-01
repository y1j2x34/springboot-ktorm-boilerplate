package com.vgerbot.common.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp

/**
 * 可审计表抽象类
 * 提供通用审计字段的列定义
 * 
 * @param E 实体类型，必须实现 AuditableEntity
 * @param tableName 表名
 */
abstract class AuditableTable<E : AuditableEntity<E>>(tableName: String) : Table<E>(tableName) {
    
    /**
     * 创建人ID列
     */
    val createdBy = int("created_by").bindTo { it.createdBy }
    
    /**
     * 创建时间列
     */
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    
    /**
     * 更新人ID列
     */
    val updatedBy = int("updated_by").bindTo { it.updatedBy }
    
    /**
     * 更新时间列
     */
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    
    /**
     * 逻辑删除标志列
     */
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}


