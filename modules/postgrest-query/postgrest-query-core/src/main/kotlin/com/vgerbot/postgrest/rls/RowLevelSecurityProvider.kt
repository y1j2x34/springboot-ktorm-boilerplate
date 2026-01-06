package com.vgerbot.postgrest.rls

import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.postgrest.builder.RlsCondition
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 行级安全（RLS）提供者
 * 基于 authorization 模块实现 RLS 策略
 */
@Component
class RowLevelSecurityProvider(
    private val authorizationService: AuthorizationService
) {
    
    private val logger = LoggerFactory.getLogger(RowLevelSecurityProvider::class.java)
    
    /**
     * 获取 RLS 条件
     * 
     * @param tableName 表名
     * @param userId 用户ID
     * @param tenantId 租户ID（可选）
     * @param operation 操作类型（select, insert, update, delete）
     * @return RLS 条件列表
     */
    fun getRlsConditions(
        tableName: String,
        userId: String,
        tenantId: String?,
        operation: String
    ): List<RlsCondition> {
        val conditions = mutableListOf<RlsCondition>()
        
        // 检查用户是否有权限访问该表
        val hasPermission = authorizationService.enforce(
            userId,
            tableName,
            operation,
            tenantId
        )
        
        if (!hasPermission) {
            logger.warn("User {} does not have permission to {} on table {}", userId, operation, tableName)
            // 如果没有权限，返回一个永远为 false 的条件
            // 使用一个不可能满足的条件
            return listOf(RlsCondition("id", "eq", -999999))
        }
        
        // 根据操作类型添加不同的 RLS 条件
        when (operation.lowercase()) {
            "select" -> {
                // SELECT 操作：检查用户是否有权限查看数据
                // 可以添加基于租户ID的条件
                if (tenantId != null) {
                    conditions.add(RlsCondition("tenant_id", "eq", tenantId.toIntOrNull() ?: tenantId))
                }
                // 可以添加基于用户ID的条件（如果表有 created_by 字段）
                // conditions.add(RlsCondition("created_by", "eq", userId.toIntOrNull() ?: userId))
            }
            "insert" -> {
                // INSERT 操作：通常允许用户插入自己的数据
                // 无需额外条件
            }
            "update" -> {
                // UPDATE 操作：通常只允许用户更新自己的数据
                val userIdValue = userId.toIntOrNull() ?: userId
                conditions.add(RlsCondition("created_by", "eq", userIdValue))
            }
            "delete" -> {
                // DELETE 操作：通常只允许用户删除自己的数据
                val userIdValue = userId.toIntOrNull() ?: userId
                conditions.add(RlsCondition("created_by", "eq", userIdValue))
            }
        }
        
        logger.debug("Generated RLS conditions for table {}: {}", tableName, conditions)
        
        return conditions
    }
    
    /**
     * 检查用户是否有权限执行操作
     */
    fun hasPermission(
        tableName: String,
        userId: String,
        tenantId: String?,
        operation: String
    ): Boolean {
        return authorizationService.enforce(
            userId,
            tableName,
            operation,
            tenantId
        )
    }
}

