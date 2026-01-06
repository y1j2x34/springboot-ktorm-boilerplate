package com.vgerbot.common.entity

import org.ktorm.entity.Entity
import java.time.Instant

/**
 * 可审计实体接口
 * 包含通用的审计字段：创建人、创建时间、更新人、更新时间、逻辑删除标志
 */
interface AuditableEntity<E : Entity<E>> : Entity<E> {
    /**
     * 创建人ID
     */
    var createdBy: Int?
    
    /**
     * 创建时间
     */
    var createdAt: Instant
    
    /**
     * 更新人ID
     */
    var updatedBy: Int?
    
    /**
     * 更新时间
     */
    var updatedAt: Instant?
    
    /**
     * 逻辑删除标志：false-未删除，true-已删除
     */
    var isDeleted: Boolean
}


