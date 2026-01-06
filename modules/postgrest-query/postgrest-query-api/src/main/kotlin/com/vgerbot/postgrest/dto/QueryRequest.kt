package com.vgerbot.postgrest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * PostgREST 查询请求 DTO
 * 支持通过 JSON 配置进行动态查询
 */
data class QueryRequest(
    /**
     * 表名
     */
    @field:NotBlank(message = "表名不能为空")
    val from: String,
    
    /**
     * 操作类型
     */
    @field:NotNull(message = "操作类型不能为空")
    val operation: QueryOperation,
    
    /**
     * 查询字段（select 操作使用）
     */
    val select: List<String>? = null,
    
    /**
     * 查询条件
     * 支持嵌套和 "and"/"or" 连接
     * 格式: [["field", "operator", value], "and", ["field2", "eq", value2]]
     */
    val where: List<Any>? = null,
    
    /**
     * 排序配置
     * 格式: { "field": { "ascending": true, "nullsFirst": false } }
     */
    val order: Map<String, OrderConfig>? = null,
    
    /**
     * 限制返回数量
     */
    val limit: Int? = null,
    
    /**
     * 分页范围 [from, to]
     */
    val range: List<Int>? = null,
    
    /**
     * 计数类型
     */
    val count: CountType? = null,
    
    /**
     * 是否只返回计数（不返回数据）
     */
    val head: Boolean? = false,
    
    /**
     * 插入/更新的数据
     */
    val data: Any? = null,
    
    /**
     * 冲突字段（upsert 使用）
     */
    @JsonProperty("onConflict")
    val onConflict: String? = null
)

/**
 * 查询操作类型
 */
enum class QueryOperation {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    UPSERT
}

/**
 * 排序配置
 */
data class OrderConfig(
    /**
     * 是否升序
     */
    val ascending: Boolean = true,
    
    /**
     * null 值是否排在前面
     */
    val nullsFirst: Boolean? = null
)

/**
 * 计数类型
 */
enum class CountType {
    EXACT,
    ESTIMATED
}

