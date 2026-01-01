package com.vgerbot.rbac.controller

import com.vgerbot.rbac.dto.*
import com.vgerbot.rbac.service.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/permissions")
class PermissionController {
    
    @Autowired
    lateinit var permissionService: PermissionService
    
    @PostMapping
    fun createPermission(@RequestBody dto: CreatePermissionDto): ResponseEntity<Any> {
        val permission = permissionService.createPermission(dto)
        return if (permission != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(permission)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Permission code already exists"))
        }
    }
    
    @PutMapping("/{id}")
    fun updatePermission(@PathVariable id: Int, @RequestBody dto: UpdatePermissionDto): ResponseEntity<Any> {
        val updated = permissionService.updatePermission(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Permission updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Int): ResponseEntity<Any> {
        val deleted = permissionService.deletePermission(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Permission deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @GetMapping("/{id}")
    fun getPermissionById(@PathVariable id: Int): ResponseEntity<Any> {
        val permission = permissionService.getPermissionById(id)
        return if (permission != null) {
            ResponseEntity.ok(permission)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @GetMapping("/code/{code}")
    fun getPermissionByCode(@PathVariable code: String): ResponseEntity<Any> {
        val permission = permissionService.getPermissionByCode(code)
        return if (permission != null) {
            ResponseEntity.ok(permission)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @GetMapping
    fun getAllPermissions(): ResponseEntity<List<PermissionDto>> {
        val permissions = permissionService.getAllPermissions()
        return ResponseEntity.ok(permissions)
    }
}

