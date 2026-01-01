package com.vgerbot.common.entity

import org.ktorm.entity.Entity
import java.time.Instant

/**
 * 带状态的审计实体接口（不使用逻辑删除）
 * 
 * 适用于配置类数据（如角色、权限、字典等），这类数据通常不需要逻辑删除，
 * 而是使用 status 字段来控制启用/停用状态
 * 
 * 包含字段：
 * - createdBy: 创建人ID
 * - createdAt: 创建时间
 * - updatedBy: 更新人ID
 * - updatedAt: 更新时间
 * - status: 状态（通常 0-停用, 1-启用）
 */
interface StatusAuditableEntity<E : Entity<E>> : Entity<E> {
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
     * 状态：通常 0-停用, 1-启用
     */
    var status: Int
}

