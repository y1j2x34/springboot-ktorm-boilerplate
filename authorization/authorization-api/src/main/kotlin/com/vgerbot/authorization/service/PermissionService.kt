package com.vgerbot.authorization.service

import com.vgerbot.authorization.dto.CreatePermissionDto
import com.vgerbot.authorization.dto.PermissionDto
import com.vgerbot.authorization.dto.UpdatePermissionDto

interface PermissionService {
    fun createPermission(dto: CreatePermissionDto): PermissionDto?
    fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean
    fun deletePermission(id: Int): Boolean
    fun getPermissionById(id: Int): PermissionDto?
    fun getPermissionByCode(code: String): PermissionDto?
    fun getAllPermissions(): List<PermissionDto>
}

