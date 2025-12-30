package com.vgerbot.rbac.service

import com.vgerbot.rbac.dto.CreatePermissionDto
import com.vgerbot.rbac.dto.UpdatePermissionDto
import com.vgerbot.rbac.model.Permission

interface PermissionService {
    fun createPermission(dto: CreatePermissionDto): Permission?
    fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean
    fun deletePermission(id: Int): Boolean
    fun getPermissionById(id: Int): Permission?
    fun getPermissionByCode(code: String): Permission?
    fun getAllPermissions(): List<Permission>
}

