package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.PermissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Permission Controller
 * Provides REST API for permission management
 */
@Tag(name = "Permission", description = "Permission management APIs")
@RestController
@RequestMapping("/permissions")
class PermissionController {
    
    @Autowired
    lateinit var permissionService: PermissionService
    
    @Operation(summary = "Create permission", description = "Create a new permission")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Permission created successfully"),
        ApiResponse(responseCode = "409", description = "Permission code already exists")
    )
    @PostMapping
    fun createPermission(
        @Parameter(description = "Permission creation data", required = true)
        @RequestBody dto: CreatePermissionDto
    ): ResponseEntity<Any> {
        val permission = permissionService.createPermission(dto)
        return if (permission != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(permission)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Permission code already exists"))
        }
    }
    
    @Operation(summary = "Update permission", description = "Update an existing permission")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission updated successfully"),
        ApiResponse(responseCode = "404", description = "Permission not found")
    )
    @PutMapping("/{id}")
    fun updatePermission(
        @Parameter(description = "Permission ID", required = true)
        @PathVariable id: Int,
        @Parameter(description = "Permission update data", required = true)
        @RequestBody dto: UpdatePermissionDto
    ): ResponseEntity<Any> {
        val updated = permissionService.updatePermission(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Permission updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @Operation(summary = "Delete permission", description = "Delete a permission")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission deleted successfully"),
        ApiResponse(responseCode = "404", description = "Permission not found")
    )
    @DeleteMapping("/{id}")
    fun deletePermission(
        @Parameter(description = "Permission ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        val deleted = permissionService.deletePermission(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Permission deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @Operation(summary = "Get permission by ID", description = "Retrieve a permission by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission found"),
        ApiResponse(responseCode = "404", description = "Permission not found")
    )
    @GetMapping("/{id}")
    fun getPermissionById(
        @Parameter(description = "Permission ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        val permission = permissionService.getPermissionById(id)
        return if (permission != null) {
            ResponseEntity.ok(permission)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @Operation(summary = "Get permission by code", description = "Retrieve a permission by its code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission found"),
        ApiResponse(responseCode = "404", description = "Permission not found")
    )
    @GetMapping("/code/{code}")
    fun getPermissionByCode(
        @Parameter(description = "Permission code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Any> {
        val permission = permissionService.getPermissionByCode(code)
        return if (permission != null) {
            ResponseEntity.ok(permission)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Permission not found"))
        }
    }
    
    @Operation(summary = "Get all permissions", description = "Retrieve a list of all permissions")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions")
    @GetMapping
    fun getAllPermissions(): ResponseEntity<List<PermissionDto>> {
        val permissions = permissionService.getAllPermissions()
        return ResponseEntity.ok(permissions)
    }
}

