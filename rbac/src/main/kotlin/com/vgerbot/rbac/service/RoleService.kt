package com.vgerbot.rbac.service

import com.vgerbot.rbac.dao.RoleDao
import com.vgerbot.rbac.dto.CreateRoleDto
import com.vgerbot.rbac.dto.RoleDto
import com.vgerbot.rbac.dto.UpdateRoleDto
import com.vgerbot.rbac.model.Role
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface RoleService {
    fun createRole(dto: CreateRoleDto): Role?
    fun updateRole(id: Int, dto: UpdateRoleDto): Boolean
    fun deleteRole(id: Int): Boolean
    fun getRoleById(id: Int): Role?
    fun getRoleByCode(code: String): Role?
    fun getAllRoles(): List<Role>
}

@Service
class RoleServiceImpl : RoleService {
    
    @Autowired
    lateinit var roleDao: RoleDao
    
    @Transactional
    override fun createRole(dto: CreateRoleDto): Role? {
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
        
        return if (roleDao.add(role) == 1) role else null
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
    
    override fun getRoleById(id: Int): Role? {
        return roleDao.findOne { it.id eq id }
    }
    
    override fun getRoleByCode(code: String): Role? {
        return roleDao.findOne { it.code eq code }
    }
    
    override fun getAllRoles(): List<Role> {
        return roleDao.findAll()
    }
}

