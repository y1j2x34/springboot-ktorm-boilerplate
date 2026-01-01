package com.vgerbot.rbac.dto

import java.time.Instant
import java.time.LocalDateTime

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

