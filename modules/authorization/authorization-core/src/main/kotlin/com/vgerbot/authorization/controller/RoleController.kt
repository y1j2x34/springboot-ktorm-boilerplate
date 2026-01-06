package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.RoleService
import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Role Controller
 * Provides REST API for role management
 */
@Tag(name = "Role", description = "Role management APIs")
@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {
    
    @Operation(summary = "Create role", description = "Create a new role")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Role created successfully"),
        ApiResponse(responseCode = "409", description = "Role code already exists")
    )
    @PostMapping
    fun createRole(
        @Parameter(description = "Role creation data", required = true)
        @RequestBody dto: CreateRoleDto
    ): ResponseEntity<Map<String, Any>> {
        val role = roleService.createRole(dto)
            ?: throw ConflictException("角色代码已存在")
        return role.created("角色创建成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val updated = roleService.updateRole(id, dto)
        if (!updated) {
            throw NotFoundException("角色不存在")
        }
        return ok("角色更新成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val deleted = roleService.deleteRole(id)
        if (!deleted) {
            throw NotFoundException("角色不存在")
        }
        return ok("角色删除成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val role = roleService.getRoleById(id)
            ?: throw NotFoundException("角色不存在")
        return role.ok()
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
    ): ResponseEntity<Map<String, Any>> {
        val role = roleService.getRoleByCode(code)
            ?: throw NotFoundException("角色不存在")
        return role.ok()
    }
    
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved roles")
    @GetMapping
    fun getAllRoles(): ResponseEntity<Map<String, Any>> {
        val roles = roleService.getAllRoles()
        return roles.ok()
    }
}

