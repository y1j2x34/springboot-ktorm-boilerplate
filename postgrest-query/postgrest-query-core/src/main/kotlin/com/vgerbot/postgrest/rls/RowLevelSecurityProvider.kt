package com.vgerbot.postgrest.rls

import com.vgerbot.authorization.api.AuthorizationService
import org.slf4j.LoggerFactory

/**
 * 行级安全（RLS）提供者
 * 基于 authorization 模块实现 RLS 策略
 */
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
     * @return RLS 条件列表（SQL WHERE 子句片段）
     */
    fun getRlsConditions(
        tableName: String,
        userId: String,
        tenantId: String?,
        operation: String
    ): List<String> {
        val conditions = mutableListOf<String>()
        
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
            return listOf("1 = 0")
        }
        
        // 根据操作类型添加不同的 RLS 条件
        when (operation.lowercase()) {
            "select" -> {
                // SELECT 操作：检查用户是否有权限查看数据
                // 可以添加基于用户ID或租户ID的条件
                if (tenantId != null) {
                    conditions.add("tenant_id = '$tenantId'")
                }
                // 可以添加基于用户ID的条件（如果表有 created_by 字段）
                // conditions.add("(created_by = '$userId' OR is_public = true)")
            }
            "insert" -> {
                // INSERT 操作：检查用户是否有权限插入数据
                // 通常允许用户插入自己的数据
            }
            "update" -> {
                // UPDATE 操作：检查用户是否有权限更新数据
                // 通常只允许用户更新自己的数据
                conditions.add("created_by = '$userId'")
            }
            "delete" -> {
                // DELETE 操作：检查用户是否有权限删除数据
                // 通常只允许用户删除自己的数据
                conditions.add("created_by = '$userId'")
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

