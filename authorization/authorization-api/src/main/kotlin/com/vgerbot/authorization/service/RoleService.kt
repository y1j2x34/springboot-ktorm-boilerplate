package com.vgerbot.authorization.service

import com.vgerbot.authorization.dto.CreateRoleDto
import com.vgerbot.authorization.dto.RoleDto
import com.vgerbot.authorization.dto.UpdateRoleDto

interface RoleService {
    fun createRole(dto: CreateRoleDto): RoleDto?
    fun updateRole(id: Int, dto: UpdateRoleDto): Boolean
    fun deleteRole(id: Int): Boolean
    fun getRoleById(id: Int): RoleDto?
    fun getRoleByCode(code: String): RoleDto?
    fun getAllRoles(): List<RoleDto>
}

