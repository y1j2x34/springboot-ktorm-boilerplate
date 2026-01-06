package com.vgerbot.postgrest.service

import com.vgerbot.common.exception.BusinessException
import com.vgerbot.postgrest.api.PostgrestQueryService
import com.vgerbot.postgrest.builder.KtormQueryBuilder
import com.vgerbot.postgrest.dto.QueryRequest
import com.vgerbot.postgrest.dto.QueryResponse
import com.vgerbot.postgrest.rls.RowLevelSecurityProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * PostgREST 查询服务实现
 * 使用 Ktorm 构建和执行查询，确保数据库兼容性
 */
@Service
class PostgrestQueryServiceImpl(
    private val queryBuilder: KtormQueryBuilder,
    private val rlsProvider: RowLevelSecurityProvider
) : PostgrestQueryService {
    
    private val logger = LoggerFactory.getLogger(PostgrestQueryServiceImpl::class.java)
    
    override fun executeQuery(
        request: QueryRequest,
        userId: String,
        tenantId: String?
    ): QueryResponse {
        logger.info("Executing query: table={}, operation={}, user={}, tenant={}", 
            request.from, request.operation, userId, tenantId)
        
        // 检查权限
        val operation = request.operation.name.lowercase()
        val hasPermission = rlsProvider.hasPermission(
            request.from,
            userId,
            tenantId,
            operation
        )
        
        if (!hasPermission) {
            throw BusinessException(
                "没有权限执行 ${operation} 操作 on 表 ${request.from}",
                code = 403
            )
        }
        
        // 获取 RLS 条件
        val rlsConditions = rlsProvider.getRlsConditions(
            request.from,
            userId,
            tenantId,
            operation
        )
        
        // 使用 Ktorm 执行查询
        try {
            val result = queryBuilder.buildAndExecute(request, rlsConditions)
            
            return QueryResponse(
                data = result.data,
                count = result.count,
                head = result.headOnly
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error executing query", e)
            throw BusinessException(
                "查询执行失败: ${e.message}",
                code = 500,
                cause = e
            )
        }
    }
}

