package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.RbacDataService
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
 * RBAC Data Controller
 * Manages user-role associations, role-permission associations, and user direct permissions (ACL)
 */
@Tag(name = "RBAC Data", description = "RBAC data management APIs")
@RestController
@RequestMapping("/rbac")
class RbacDataController(
    private val rbacDataService: RbacDataService
) {
    
    // ==================== 用户角色管理 ====================
    
    @Operation(summary = "Assign role to user", description = "Assign a role to a user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role assigned successfully"),
        ApiResponse(responseCode = "409", description = "Role already assigned to user")
    )
    @PostMapping("/users/roles")
    fun assignRoleToUser(
        @Parameter(description = "Role assignment data", required = true)
        @RequestBody dto: AssignRoleToUserDto
    ): ResponseEntity<Map<String, Any>> {
        val assigned = rbacDataService.assignRoleToUser(dto.userId, dto.roleId)
        if (!assigned) {
            throw ConflictException("角色已分配给用户")
        }
        return ok("角色分配成功")
    }
    
    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Role removed successfully"),
        ApiResponse(responseCode = "404", description = "User role assignment not found")
    )
    @DeleteMapping("/users/roles")
    fun removeRoleFromUser(
        @Parameter(description = "Role removal data", required = true)
        @RequestBody dto: RemoveRoleFromUserDto
    ): ResponseEntity<Map<String, Any>> {
        val removed = rbacDataService.removeRoleFromUser(dto.userId, dto.roleId)
        if (!removed) {
            throw NotFoundException("用户角色关联不存在")
        }
        return ok("角色移除成功")
    }
    
    @Operation(summary = "Get user roles", description = "Retrieve all roles assigned to a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user roles")
    @GetMapping("/users/{userId}/roles")
    fun getUserRoles(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int
    ): ResponseEntity<Map<String, Any>> {
        val roles = rbacDataService.getUserRoles(userId)
        return roles.ok()
    }
    
    @Operation(summary = "Get user permissions", description = "Retrieve all permissions for a user (including role-based and direct permissions)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user permissions")
    @GetMapping("/users/{userId}/permissions")
    fun getUserPermissions(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int
    ): ResponseEntity<Map<String, Any>> {
        val permissions = rbacDataService.getUserPermissions(userId)
        return permissions.ok()
    }
    
    // ==================== 角色权限管理 ====================
    
    @Operation(summary = "Assign permission to role", description = "Assign a permission to a role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission assigned successfully"),
        ApiResponse(responseCode = "409", description = "Permission already assigned to role")
    )
    @PostMapping("/roles/permissions")
    fun assignPermissionToRole(
        @Parameter(description = "Permission assignment data", required = true)
        @RequestBody dto: AssignPermissionToRoleDto
    ): ResponseEntity<Map<String, Any>> {
        val assigned = rbacDataService.assignPermissionToRole(dto.roleId, dto.permissionId)
        if (!assigned) {
            throw ConflictException("权限已分配给角色")
        }
        return ok("权限分配成功")
    }
    
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission removed successfully"),
        ApiResponse(responseCode = "404", description = "Role permission assignment not found")
    )
    @DeleteMapping("/roles/permissions")
    fun removePermissionFromRole(
        @Parameter(description = "Permission removal data", required = true)
        @RequestBody dto: RemovePermissionFromRoleDto
    ): ResponseEntity<Map<String, Any>> {
        val removed = rbacDataService.removePermissionFromRole(dto.roleId, dto.permissionId)
        if (!removed) {
            throw NotFoundException("角色权限关联不存在")
        }
        return ok("权限移除成功")
    }
    
    @Operation(summary = "Get role permissions", description = "Retrieve all permissions assigned to a role")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved role permissions")
    @GetMapping("/roles/{roleId}/permissions")
    fun getRolePermissions(
        @Parameter(description = "Role ID", required = true)
        @PathVariable roleId: Int
    ): ResponseEntity<Map<String, Any>> {
        val permissions = rbacDataService.getRolePermissions(roleId)
        return permissions.ok()
    }
    
    // ==================== ACL: 用户直接权限管理 ====================
    
    @Operation(summary = "Assign permission to user", description = "Assign a direct permission to a user (ACL)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission assigned successfully"),
        ApiResponse(responseCode = "409", description = "Permission already assigned to user")
    )
    @PostMapping("/users/permissions")
    fun assignPermissionToUser(
        @Parameter(description = "Permission assignment data", required = true)
        @RequestBody dto: AssignPermissionToUserDto
    ): ResponseEntity<Map<String, Any>> {
        val assigned = rbacDataService.assignPermissionToUser(dto.userId, dto.permissionId, dto.tenantId)
        if (!assigned) {
            throw ConflictException("权限已分配给用户")
        }
        return ok("权限分配成功")
    }
    
    @Operation(summary = "Remove permission from user", description = "Remove a direct permission from a user (ACL)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permission removed successfully"),
        ApiResponse(responseCode = "404", description = "User permission assignment not found")
    )
    @DeleteMapping("/users/permissions")
    fun removePermissionFromUser(
        @Parameter(description = "Permission removal data", required = true)
        @RequestBody dto: RemovePermissionFromUserDto
    ): ResponseEntity<Map<String, Any>> {
        val removed = rbacDataService.removePermissionFromUser(dto.userId, dto.permissionId, dto.tenantId)
        if (!removed) {
            throw NotFoundException("用户权限关联不存在")
        }
        return ok("权限移除成功")
    }
    
    @Operation(summary = "Get user direct permissions", description = "Retrieve direct permissions assigned to a user (ACL, excluding role-based permissions)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved direct permissions")
    @GetMapping("/users/{userId}/direct-permissions")
    fun getUserDirectPermissions(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Optional tenant ID filter")
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<Map<String, Any>> {
        val permissions = rbacDataService.getUserDirectPermissions(userId, tenantId)
        return permissions.ok()
    }
    
    @Operation(summary = "Get all user permissions", description = "Retrieve all permissions for a user (including role-based and direct permissions)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all permissions")
    @GetMapping("/users/{userId}/all-permissions")
    fun getAllUserPermissions(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Optional tenant ID filter")
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<Map<String, Any>> {
        val permissions = rbacDataService.getAllUserPermissions(userId, tenantId)
        return permissions.ok()
    }
    
    // ==================== 权限检查 ====================
    
    @Operation(summary = "Check user permission", description = "Check if a user has a specific permission")
    @ApiResponse(responseCode = "200", description = "Permission check completed")
    @GetMapping("/users/{userId}/has-permission/{permissionCode}")
    fun hasPermission(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Permission code", required = true)
        @PathVariable permissionCode: String
    ): ResponseEntity<Map<String, Any>> {
        val hasPermission = rbacDataService.hasPermissionInDb(userId, permissionCode)
        return mapOf("hasPermission" to hasPermission).ok()
    }
    
    @Operation(summary = "Check user role", description = "Check if a user has a specific role")
    @ApiResponse(responseCode = "200", description = "Role check completed")
    @GetMapping("/users/{userId}/has-role/{roleCode}")
    fun hasRole(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Role code", required = true)
        @PathVariable roleCode: String
    ): ResponseEntity<Map<String, Any>> {
        val hasRole = rbacDataService.hasRoleInDb(userId, roleCode)
        return mapOf("hasRole" to hasRole).ok()
    }
    
    @Operation(summary = "Check user direct permission", description = "Check if a user has a specific direct permission (ACL)")
    @ApiResponse(responseCode = "200", description = "Direct permission check completed")
    @GetMapping("/users/{userId}/has-direct-permission/{permissionCode}")
    fun hasDirectPermission(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Permission code", required = true)
        @PathVariable permissionCode: String,
        @Parameter(description = "Optional tenant ID filter")
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<Map<String, Any>> {
        val hasPermission = rbacDataService.hasDirectPermissionInDb(userId, permissionCode, tenantId)
        return mapOf("hasDirectPermission" to hasPermission).ok()
    }
}
