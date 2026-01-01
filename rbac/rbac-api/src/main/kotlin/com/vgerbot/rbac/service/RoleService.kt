package com.vgerbot.rbac.service

import com.vgerbot.rbac.dto.CreateRoleDto
import com.vgerbot.rbac.dto.RoleDto
import com.vgerbot.rbac.dto.UpdateRoleDto

interface RoleService {
    fun createRole(dto: CreateRoleDto): RoleDto?
    fun updateRole(id: Int, dto: UpdateRoleDto): Boolean
    fun deleteRole(id: Int): Boolean
    fun getRoleById(id: Int): RoleDto?
    fun getRoleByCode(code: String): RoleDto?
    fun getAllRoles(): List<RoleDto>
}

