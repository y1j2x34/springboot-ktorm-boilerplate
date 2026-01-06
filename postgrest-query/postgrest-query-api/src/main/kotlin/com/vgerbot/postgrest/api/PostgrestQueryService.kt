package com.vgerbot.postgrest.api

import com.vgerbot.postgrest.dto.QueryRequest
import com.vgerbot.postgrest.dto.QueryResponse

/**
 * PostgREST 查询服务接口
 */
interface PostgrestQueryService {
    
    /**
     * 执行查询请求
     * 
     * @param request 查询请求
     * @param userId 当前用户ID（用于 RLS）
     * @param tenantId 当前租户ID（用于 RLS，可选）
     * @return 查询结果
     */
    fun executeQuery(
        request: QueryRequest,
        userId: String,
        tenantId: String? = null
    ): QueryResponse
}

