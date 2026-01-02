package com.vgerbot.authorization.dto

import java.time.Instant

data class RoleDto(
    val id: Int? = null,
    val name: String,
    val code: String,
    val description: String? = null,
    val createdBy: Int? = null,
    val createdAt: Instant? = null,
    val updatedBy: Int? = null,
    val updatedAt: Instant? = null
)

data class CreateRoleDto(
    val name: String,
    val code: String,
    val description: String? = null
)

data class UpdateRoleDto(
    val name: String? = null,
    val code: String? = null,
    val description: String? = null
)

data class AssignRoleToUserDto(
    val userId: Int,
    val roleId: Int
)

data class RemoveRoleFromUserDto(
    val userId: Int,
    val roleId: Int
)

