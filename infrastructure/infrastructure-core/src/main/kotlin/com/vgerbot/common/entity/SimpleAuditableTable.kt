package com.vgerbot.common.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.timestamp

/**
 * 简化的审计表抽象类，适用于关联表等只需要记录创建时间和删除标记的场景
 * 
 * 此类自动绑定以下字段到数据库列：
 * - created_at -> createdAt
 * - is_deleted -> isDeleted
 * 
 * @param E 实体类型，必须实现 SimpleAuditableEntity 接口
 * @param tableName 数据库表名
 */
abstract class SimpleAuditableTable<E : SimpleAuditableEntity<E>>(tableName: String) : Table<E>(tableName) {
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}

