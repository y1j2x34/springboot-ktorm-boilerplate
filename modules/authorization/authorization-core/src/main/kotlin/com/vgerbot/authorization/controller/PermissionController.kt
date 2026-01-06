package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.PermissionService
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
 * Permission Controller
 * Provides REST API for permission management
 */
@Tag(name = "Permission", description = "Permission management APIs")
@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {
    
    @Operation(summary = "Create permission", description = "Create a new permission")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Permission created successfully"),
        ApiResponse(responseCode = "409", description = "Permission code already exists")
    )
    @PostMapping
    fun createPermission(
        @Parameter(description = "Permission creation data", required = true)
        @RequestBody dto: CreatePermissionDto
    ): ResponseEntity<Map<String, Any>> {
        val permission = permissionService.createPermission(dto)
            ?: throw ConflictException("权限代码已存在")
        return permission.created("权限创建成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val updated = permissionService.updatePermission(id, dto)
        if (!updated) {
            throw NotFoundException("权限不存在")
        }
        return ok("权限更新成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val deleted = permissionService.deletePermission(id)
        if (!deleted) {
            throw NotFoundException("权限不存在")
        }
        return ok("权限删除成功")
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
    ): ResponseEntity<Map<String, Any>> {
        val permission = permissionService.getPermissionById(id)
            ?: throw NotFoundException("权限不存在")
        return permission.ok()
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
    ): ResponseEntity<Map<String, Any>> {
        val permission = permissionService.getPermissionByCode(code)
            ?: throw NotFoundException("权限不存在")
        return permission.ok()
    }
    
    @Operation(summary = "Get all permissions", description = "Retrieve a list of all permissions")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions")
    @GetMapping
    fun getAllPermissions(): ResponseEntity<Map<String, Any>> {
        val permissions = permissionService.getAllPermissions()
        return permissions.ok()
    }
}

