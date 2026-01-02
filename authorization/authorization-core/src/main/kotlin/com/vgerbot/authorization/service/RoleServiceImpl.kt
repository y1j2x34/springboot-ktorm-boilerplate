package com.vgerbot.authorization.service

import com.vgerbot.authorization.dao.RoleDao
import com.vgerbot.authorization.dto.CreateRoleDto
import com.vgerbot.authorization.dto.RoleDto
import com.vgerbot.authorization.dto.UpdateRoleDto
import com.vgerbot.authorization.entity.Role
import com.vgerbot.authorization.entity.toDto
import org.ktorm.dsl.and
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
        // 检查角色代码是否已存在（只查询启用的）
        val existing = roleDao.findOne { (it.code eq dto.code) and (it.status eq 1) }
        if (existing != null) {
            return null
        }
        
        val role = Role()
        role.name = dto.name
        role.code = dto.code
        role.description = dto.description
        role.createdAt = Instant.now()
        role.status = 1 // 默认启用
        
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
        val role = roleDao.findOne { it.id eq id } ?: return false
        role.status = 0 // 停用而不是删除
        role.updatedAt = Instant.now()
        return roleDao.update(role) == 1
    }
    
    override fun getRoleById(id: Int): RoleDto? {
        return roleDao.findOne { (it.id eq id) and (it.status eq 1) }?.toDto()
    }
    
    override fun getRoleByCode(code: String): RoleDto? {
        return roleDao.findOne { (it.code eq code) and (it.status eq 1) }?.toDto()
    }
    
    override fun getAllRoles(): List<RoleDto> {
        return roleDao.findList { it.status eq 1 }.map { it.toDto() }
    }
}

