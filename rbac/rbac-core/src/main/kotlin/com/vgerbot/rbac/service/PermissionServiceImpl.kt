package com.vgerbot.rbac.service

import com.vgerbot.rbac.dao.PermissionDao
import com.vgerbot.rbac.dto.CreatePermissionDto
import com.vgerbot.rbac.dto.PermissionDto
import com.vgerbot.rbac.dto.UpdatePermissionDto
import com.vgerbot.rbac.entity.Permission
import com.vgerbot.rbac.entity.toDto
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PermissionServiceImpl : PermissionService {
    
    @Autowired
    lateinit var permissionDao: PermissionDao
    
    @Transactional
    override fun createPermission(dto: CreatePermissionDto): PermissionDto? {
        // 检查权限代码是否已存在（只查询未删除的）
        val existing = permissionDao.findOneActive { it.code eq dto.code }
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
        permission.isDeleted = false
        
        return if (permissionDao.add(permission) == 1) permission.toDto() else null
    }
    
    @Transactional
    override fun updatePermission(id: Int, dto: UpdatePermissionDto): Boolean {
        val permission = permissionDao.findOneActive { it.id eq id } ?: return false
        
        dto.name?.let { permission.name = it }
        dto.code?.let { permission.code = it }
        dto.resource?.let { permission.resource = it }
        dto.action?.let { permission.action = it }
        dto.description?.let { permission.description = it }
        permission.updatedAt = Instant.now()
        
        return permissionDao.update(permission) == 1
    }
    
    @Transactional
    override fun deletePermission(id: Int): Boolean {
        return permissionDao.softDelete(id)
    }
    
    override fun getPermissionById(id: Int): PermissionDto? {
        return permissionDao.findOneActive { it.id eq id }?.toDto()
    }
    
    override fun getPermissionByCode(code: String): PermissionDto? {
        return permissionDao.findOneActive { it.code eq code }?.toDto()
    }
    
    override fun getAllPermissions(): List<PermissionDto> {
        return permissionDao.findAllActive().map { it.toDto() }
    }
}


