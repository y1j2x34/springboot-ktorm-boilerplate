package com.vgerbot.rbac.service

import com.vgerbot.rbac.dto.CreateRoleDto
import com.vgerbot.rbac.dto.UpdateRoleDto
import com.vgerbot.rbac.model.Role

interface RoleService {
    fun createRole(dto: CreateRoleDto): Role?
    fun updateRole(id: Int, dto: UpdateRoleDto): Boolean
    fun deleteRole(id: Int): Boolean
    fun getRoleById(id: Int): Role?
    fun getRoleByCode(code: String): Role?
    fun getAllRoles(): List<Role>
}

