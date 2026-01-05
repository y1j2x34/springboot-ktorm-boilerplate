package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.RoleService
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
 * Role Controller
 * Provides REST API for role management
 */
@Tag(name = "Role", description = "Role management APIs")
@RestController
@RequestMapping("/roles")
class RoleController {
    
    @Autowired
    lateinit var roleService: RoleService
    
    @Operation(summary = "Create role", description = "Create a new role")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Role created successfully"),
        ApiResponse(responseCode = "409", description = "Role code already exists")
    )
    @PostMapping
    fun createRole(
        @Parameter(description = "Role creation data", required = true)
        @RequestBody dto: CreateRoleDto
    ): ResponseEntity<Any> {
        val role = roleService.createRole(dto)
        return if (role != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(role)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Role code already exists"))
        }
    }
    
    @Operation(summary = "Update role", description = "Update an existing role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role updated successfully"),
        ApiResponse(responseCode = "404", description = "Role not found")
    )
    @PutMapping("/{id}")
    fun updateRole(
        @Parameter(description = "Role ID", required = true)
        @PathVariable id: Int,
        @Parameter(description = "Role update data", required = true)
        @RequestBody dto: UpdateRoleDto
    ): ResponseEntity<Any> {
        val updated = roleService.updateRole(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Role updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @Operation(summary = "Delete role", description = "Delete a role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role deleted successfully"),
        ApiResponse(responseCode = "404", description = "Role not found")
    )
    @DeleteMapping("/{id}")
    fun deleteRole(
        @Parameter(description = "Role ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        val deleted = roleService.deleteRole(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Role deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role found"),
        ApiResponse(responseCode = "404", description = "Role not found")
    )
    @GetMapping("/{id}")
    fun getRoleById(
        @Parameter(description = "Role ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        val role = roleService.getRoleById(id)
        return if (role != null) {
            ResponseEntity.ok(role)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @Operation(summary = "Get role by code", description = "Retrieve a role by its code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role found"),
        ApiResponse(responseCode = "404", description = "Role not found")
    )
    @GetMapping("/code/{code}")
    fun getRoleByCode(
        @Parameter(description = "Role code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Any> {
        val role = roleService.getRoleByCode(code)
        return if (role != null) {
            ResponseEntity.ok(role)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role not found"))
        }
    }
    
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved roles")
    @GetMapping
    fun getAllRoles(): ResponseEntity<List<RoleDto>> {
        val roles = roleService.getAllRoles()
        return ResponseEntity.ok(roles)
    }
}

