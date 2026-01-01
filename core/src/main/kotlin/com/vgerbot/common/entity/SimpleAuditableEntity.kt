package com.vgerbot.common.entity

import org.ktorm.entity.Entity
import java.time.Instant

/**
 * 简化的审计实体接口，适用于关联表等只需要记录创建时间和删除标记的场景
 * 
 * 此接口定义了两个基本审计字段：
 * - createdAt: 创建时间
 * - isDeleted: 逻辑删除标记
 */
interface SimpleAuditableEntity<E : Entity<E>> : Entity<E> {
    var createdAt: Instant
    var isDeleted: Boolean
}

