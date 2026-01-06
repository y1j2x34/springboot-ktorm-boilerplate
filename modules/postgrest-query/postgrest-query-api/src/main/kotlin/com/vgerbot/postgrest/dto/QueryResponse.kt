package com.vgerbot.postgrest.dto

/**
 * PostgREST 查询响应 DTO
 */
data class QueryResponse(
    /**
     * 查询结果数据
     */
    val data: List<Map<String, Any?>>? = null,
    
    /**
     * 总记录数（当 count 参数存在时）
     */
    val count: Int? = null,
    
    /**
     * 是否只返回计数
     */
    val head: Boolean = false
)

