package com.vgerbot.rbac.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.rbac.dto.*
import com.vgerbot.rbac.service.RoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
class RoleController {
    
    @Autowired
    lateinit var roleService: RoleService
    
    @PostMapping
    fun createRole(@RequestBody dto: CreateRoleDto): ResponseEntity<Map<String, Any>> {
        val role = roleService.createRole(dto)
            ?: throw ConflictException("角色代码已存在")
        return role.created("角色创建成功")
    }
    
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Int, @RequestBody dto: UpdateRoleDto): ResponseEntity<Map<String, Any>> {
        val updated = roleService.updateRole(id, dto)
        if (!updated) {
            throw NotFoundException("角色不存在")
        }
        return ok("角色更新成功")
    }
    
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val deleted = roleService.deleteRole(id)
        if (!deleted) {
            throw NotFoundException("角色不存在")
        }
        return ok("角色删除成功")
    }
    
    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val role = roleService.getRoleById(id)
            ?: throw NotFoundException("角色不存在")
        return role.ok()
    }
    
    @GetMapping("/code/{code}")
    fun getRoleByCode(@PathVariable code: String): ResponseEntity<Map<String, Any>> {
        val role = roleService.getRoleByCode(code)
            ?: throw NotFoundException("角色不存在")
        return role.ok()
    }
    
    @GetMapping
    fun getAllRoles(): ResponseEntity<Map<String, Any>> {
        val roles = roleService.getAllRoles()
        return roles.ok()
    }
}

