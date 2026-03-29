package com.vgerbot.auth.common.dto

/**
 * 认证声明的标准化模型。
 *
 * 不同认证提供方返回的 claims 结构可能不同，
 * 认证模块应先转换为该 DTO，再进行本地用户映射与 Principal 组装。
 */
data class TokenClaimsDto(
    val provider: String,
    val subject: String,
    val username: String? = null,
    val email: String? = null,
    val tenantId: String? = null,
    val organizationId: String? = null,
    val authorities: List<String> = emptyList(),
    val attributes: Map<String, Any?> = emptyMap()
)
