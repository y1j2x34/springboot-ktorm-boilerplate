package com.vgerbot.rbac.service

import com.vgerbot.rbac.dto.CreatePermissionDto
import com.vgerbot.rbac.dto.PermissionDto
import com.vgerbot.rbac.dto.UpdatePermissionDto

interface PermissionService {
    fun createPermission(dto: CreatePermissionDto): PermissionDto?
    fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean
    fun deletePermission(id: Int): Boolean
    fun getPermissionById(id: Int): PermissionDto?
    fun getPermissionByCode(code: String): PermissionDto?
    fun getAllPermissions(): List<PermissionDto>
}

