package com.vgerbot.rbac.controller

import com.vgerbot.rbac.dto.*
import com.vgerbot.rbac.model.Role
import com.vgerbot.rbac.service.RoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
class RoleController {
    
    @Autowired
    lateinit var roleService: RoleService
    
    @PostMapping
    fun createRole(@RequestBody dto: CreateRoleDto): ResponseEntity<Any> {
        val role = roleService.createRole(dto)
        return if (role != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(role)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Role code already exists"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Int, @RequestBody dto: UpdateRoleDto): ResponseEntity<Any> {
        val updated = roleService.updateRole(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Role updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Int): ResponseEntity<Any> {
        val deleted = roleService.deleteRole(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Role deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Int): ResponseEntity<Any> {
        val role = roleService.getRoleById(id)
        return if (role != null) {
            ResponseEntity.ok(role)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @GetMapping("/code/{code}")
    fun getRoleByCode(@PathVariable code: String): ResponseEntity<Any> {
        val role = roleService.getRoleByCode(code)
        return if (role != null) {
            ResponseEntity.ok(role)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @GetMapping
    fun getAllRoles(): ResponseEntity<List<Role>> {
        val roles = roleService.getAllRoles()
        return ResponseEntity.ok(roles)
    }
}

