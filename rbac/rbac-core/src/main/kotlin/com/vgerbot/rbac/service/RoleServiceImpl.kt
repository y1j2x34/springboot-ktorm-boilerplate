package com.vgerbot.rbac.service

import com.vgerbot.rbac.dao.RoleDao
import com.vgerbot.rbac.dto.CreateRoleDto
import com.vgerbot.rbac.dto.RoleDto
import com.vgerbot.rbac.dto.UpdateRoleDto
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.toDto
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RoleServiceImpl : RoleService {
    
    @Autowired
    lateinit var roleDao: RoleDao
    
    @Transactional
    override fun createRole(dto: CreateRoleDto): RoleDto? {
        // 检查角色代码是否已存在
        val existing = roleDao.findOne { it.code eq dto.code }
        if (existing != null) {
            return null
        }
        
        val role = Role()
        role.name = dto.name
        role.code = dto.code
        role.description = dto.description
        role.createdAt = Instant.now()
        
        return if (roleDao.add(role) == 1) role.toDto() else null
    }
    
    @Transactional
    override fun updateRole(id: Int, dto: UpdateRoleDto): Boolean {
        val role = roleDao.findOne { it.id eq id } ?: return false
        
        dto.name?.let { role.name = it }
        dto.code?.let { role.code = it }
        dto.description?.let { role.description = it }
        role.updatedAt = Instant.now()
        
        return roleDao.update(role) == 1
    }
    
    @Transactional
    override fun deleteRole(id: Int): Boolean {
        return roleDao.deleteIf { it.id eq id } == 1
    }
    
    override fun getRoleById(id: Int): RoleDto? {
        return roleDao.findOne { it.id eq id }?.toDto()
    }
    
    override fun getRoleByCode(code: String): RoleDto? {
        return roleDao.findOne { it.code eq code }?.toDto()
    }
    
    override fun getAllRoles(): List<RoleDto> {
        return roleDao.findAll().map { it.toDto() }
    }
}

