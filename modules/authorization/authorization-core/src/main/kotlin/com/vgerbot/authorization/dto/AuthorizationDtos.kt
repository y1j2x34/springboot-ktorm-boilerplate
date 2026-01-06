package com.vgerbot.authorization.dto

/**
 * 权限检查请求
 */
data class EnforceRequest(
    val subject: String,
    val resource: String,
    val action: String,
    val domain: String? = null
)

/**
 * 权限检查响应
 */
data class EnforceResponse(
    val allowed: Boolean
)

/**
 * 批量权限检查请求
 */
data class BatchEnforceRequest(
    val requests: List<EnforceRequest>
)

/**
 * 批量权限检查响应
 */
data class BatchEnforceResponse(
    val results: List<EnforceResult>
)

data class EnforceResult(
    val request: EnforceRequest,
    val allowed: Boolean
)

/**
 * 策略请求
 */
data class PolicyRequest(
    val subject: String,
    val resource: String,
    val action: String,
    val domain: String? = null
)

/**
 * 角色分配请求
 */
data class RoleAssignmentRequest(
    val userId: Int,
    val role: String,
    val domain: String? = null
)

/**
 * 角色继承请求
 */
data class RoleInheritanceRequest(
    val role: String,
    val parentRole: String,
    val domain: String? = null
)

/**
 * 权限信息
 */
data class PermissionInfo(
    val subject: String,
    val resource: String,
    val action: String,
    val domain: String? = null
)

/**
 * 用户角色信息
 */
data class UserRoleInfo(
    val userId: Int,
    val roles: List<String>,
    val domain: String? = null
)

