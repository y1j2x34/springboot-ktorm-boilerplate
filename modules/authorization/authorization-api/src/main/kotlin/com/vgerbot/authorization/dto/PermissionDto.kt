package com.vgerbot.authorization.dto

import java.time.Instant

data class PermissionDto(
    val id: Int? = null,
    val name: String,
    val code: String,
    val resource: String,
    val action: String,
    val description: String? = null,
    val createdBy: Int? = null,
    val createdAt: Instant? = null,
    val updatedBy: Int? = null,
    val updatedAt: Instant? = null
)

data class CreatePermissionDto(
    val name: String,
    val code: String,
    val resource: String,
    val action: String,
    val description: String? = null
)

data class UpdatePermissionDto(
    val name: String? = null,
    val code: String? = null,
    val resource: String? = null,
    val action: String? = null,
    val description: String? = null
)

data class AssignPermissionToRoleDto(
    val roleId: Int,
    val permissionId: Int
)

data class RemovePermissionFromRoleDto(
    val roleId: Int,
    val permissionId: Int
)

// ACL: 用户直接权限分配 DTO
data class AssignPermissionToUserDto(
    val userId: Int,
    val permissionId: Int,
    val tenantId: Int? = null  // 可选，支持多租户 ACL
)

data class RemovePermissionFromUserDto(
    val userId: Int,
    val permissionId: Int,
    val tenantId: Int? = null  // 可选，支持多租户 ACL
)

