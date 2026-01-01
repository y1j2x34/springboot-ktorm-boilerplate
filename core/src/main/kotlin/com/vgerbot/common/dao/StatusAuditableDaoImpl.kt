package com.vgerbot.common.dao

import com.vgerbot.common.entity.StatusAuditableEntity
import com.vgerbot.common.entity.StatusAuditableTable
import org.ktorm.entity.Entity

/**
 * 带状态的审计 DAO 实现基类（不使用逻辑删除）
 * 
 * 适用于需要记录 createdAt, updatedAt, createdBy, updatedBy 和 status 的实体
 * 
 * 使用示例：
 * ```kotlin
 * @Repository
 * class RoleDaoImpl : StatusAuditableDaoImpl<Role, Roles>(Roles), RoleDao
 * ```
 */
abstract class StatusAuditableDaoImpl<E, T>(
    tableObject: T
) : AbstractBaseDao<E, T>(tableObject)
        where E : StatusAuditableEntity<E>,
              T : StatusAuditableTable<E>

