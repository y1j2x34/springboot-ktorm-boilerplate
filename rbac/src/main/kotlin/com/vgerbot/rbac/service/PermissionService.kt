package com.vgerbot.rbac.service

import com.vgerbot.rbac.dao.PermissionDao
import com.vgerbot.rbac.dto.CreatePermissionDto
import com.vgerbot.rbac.dto.UpdatePermissionDto
import com.vgerbot.rbac.model.Permission
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface PermissionService {
    fun createPermission(dto: CreatePermissionDto): Permission?
    fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean
    fun deletePermission(id: Int): Boolean
    fun getPermissionById(id: Int): Permission?
    fun getPermissionByCode(code: String): Permission?
    fun getAllPermissions(): List<Permission>
}

@Service
class PermissionServiceImpl : PermissionService {
    
    @Autowired
    lateinit var permissionDao: PermissionDao
    
    @Transactional
    override fun createPermission(dto: CreatePermissionDto): Permission? {
        // 检查权限代码是否已存在
        val existing = permissionDao.findOne { it.code eq dto.code }
        if (existing != null) {
            return null
        }
        
        val permission = Permission()
        permission.name = dto.name
        permission.code = dto.code
        permission.resource = dto.resource
        permission.action = dto.action
        permission.description = dto.description
        permission.createdAt = Instant.now()
        
        return if (permissionDao.add(permission) == 1) permission else null
    }
    
    @Transactional
    override fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean {
        val permission = permissionDao.findOne { it.id eq id } ?: return false
        
        dto.name?.let { permission.name = it }
        dto.code?.let { permission.code = it }
        dto.resource?.let { permission.resource = it }
        dto.action?.let { permission.action = it }
        dto.description?.let { permission.description = it }
        
        return permissionDao.update(permission) == 1
    }
    
    @Transactional
    override fun deletePermission(id: Int): Boolean {
        return permissionDao.deleteIf { it.id eq id } == 1
    }
    
    override fun getPermissionById(id: Int): Permission? {
        return permissionDao.findOne { it.id eq id }
    }
    
    override fun getPermissionByCode(code: String): Permission? {
        return permissionDao.findOne { it.code eq code }
    }
    
    override fun getAllPermissions(): List<Permission> {
        return permissionDao.findAll()
    }
}

