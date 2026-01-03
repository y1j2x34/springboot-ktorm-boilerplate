package com.vgerbot.authorization.controller

import com.vgerbot.authorization.dto.*
import com.vgerbot.authorization.service.RbacDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * RBAC 数据管理控制器
 * 用于管理用户角色关联、角色权限关联和用户直接权限（ACL）
 */
@RestController
@RequestMapping("/rbac")
class RbacDataController {
    
    @Autowired
    lateinit var rbacDataService: RbacDataService
    
    // ==================== 用户角色管理 ====================
    
    @PostMapping("/users/roles")
    fun assignRoleToUser(@RequestBody dto: AssignRoleToUserDto): ResponseEntity<Any> {
        val assigned = rbacDataService.assignRoleToUser(dto.userId, dto.roleId)
        return if (assigned) {
            ResponseEntity.ok(mapOf("message" to "Role assigned to user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Role already assigned to user"))
        }
    }
    
    @DeleteMapping("/users/roles")
    fun removeRoleFromUser(@RequestBody dto: RemoveRoleFromUserDto): ResponseEntity<Any> {
        val removed = rbacDataService.removeRoleFromUser(dto.userId, dto.roleId)
        return if (removed) {
            ResponseEntity.ok(mapOf("message" to "Role removed from user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "User role assignment not found"))
        }
    }
    
    @GetMapping("/users/{userId}/roles")
    fun getUserRoles(@PathVariable userId: Int): ResponseEntity<List<RoleDto>> {
        val roles = rbacDataService.getUserRoles(userId)
        return ResponseEntity.ok(roles)
    }
    
    @GetMapping("/users/{userId}/permissions")
    fun getUserPermissions(@PathVariable userId: Int): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacDataService.getUserPermissions(userId)
        return ResponseEntity.ok(permissions)
    }
    
    // ==================== 角色权限管理 ====================
    
    @PostMapping("/roles/permissions")
    fun assignPermissionToRole(@RequestBody dto: AssignPermissionToRoleDto): ResponseEntity<Any> {
        val assigned = rbacDataService.assignPermissionToRole(dto.roleId, dto.permissionId)
        return if (assigned) {
            ResponseEntity.ok(mapOf("message" to "Permission assigned to role successfully"))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Permission already assigned to role"))
        }
    }
    
    @DeleteMapping("/roles/permissions")
    fun removePermissionFromRole(@RequestBody dto: RemovePermissionFromRoleDto): ResponseEntity<Any> {
        val removed = rbacDataService.removePermissionFromRole(dto.roleId, dto.permissionId)
        return if (removed) {
            ResponseEntity.ok(mapOf("message" to "Permission removed from role successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role permission assignment not found"))
        }
    }
    
    @GetMapping("/roles/{roleId}/permissions")
    fun getRolePermissions(@PathVariable roleId: Int): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacDataService.getRolePermissions(roleId)
        return ResponseEntity.ok(permissions)
    }
    
    // ==================== ACL: 用户直接权限管理 ====================
    
    @PostMapping("/users/permissions")
    fun assignPermissionToUser(@RequestBody dto: AssignPermissionToUserDto): ResponseEntity<Any> {
        val assigned = rbacDataService.assignPermissionToUser(dto.userId, dto.permissionId, dto.tenantId)
        return if (assigned) {
            ResponseEntity.ok(mapOf("message" to "Permission assigned to user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Permission already assigned to user"))
        }
    }
    
    @DeleteMapping("/users/permissions")
    fun removePermissionFromUser(@RequestBody dto: RemovePermissionFromUserDto): ResponseEntity<Any> {
        val removed = rbacDataService.removePermissionFromUser(dto.userId, dto.permissionId, dto.tenantId)
        return if (removed) {
            ResponseEntity.ok(mapOf("message" to "Permission removed from user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "User permission assignment not found"))
        }
    }
    
    @GetMapping("/users/{userId}/direct-permissions")
    fun getUserDirectPermissions(
        @PathVariable userId: Int,
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacDataService.getUserDirectPermissions(userId, tenantId)
        return ResponseEntity.ok(permissions)
    }
    
    @GetMapping("/users/{userId}/all-permissions")
    fun getAllUserPermissions(
        @PathVariable userId: Int,
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacDataService.getAllUserPermissions(userId, tenantId)
        return ResponseEntity.ok(permissions)
    }
    
    // ==================== 权限检查 ====================
    
    @GetMapping("/users/{userId}/has-permission/{permissionCode}")
    fun hasPermission(@PathVariable userId: Int, @PathVariable permissionCode: String): ResponseEntity<Map<String, Boolean>> {
        val hasPermission = rbacDataService.hasPermissionInDb(userId, permissionCode)
        return ResponseEntity.ok(mapOf("hasPermission" to hasPermission))
    }
    
    @GetMapping("/users/{userId}/has-role/{roleCode}")
    fun hasRole(@PathVariable userId: Int, @PathVariable roleCode: String): ResponseEntity<Map<String, Boolean>> {
        val hasRole = rbacDataService.hasRoleInDb(userId, roleCode)
        return ResponseEntity.ok(mapOf("hasRole" to hasRole))
    }
    
    @GetMapping("/users/{userId}/has-direct-permission/{permissionCode}")
    fun hasDirectPermission(
        @PathVariable userId: Int, 
        @PathVariable permissionCode: String,
        @RequestParam(required = false) tenantId: Int?
    ): ResponseEntity<Map<String, Boolean>> {
        val hasPermission = rbacDataService.hasDirectPermissionInDb(userId, permissionCode, tenantId)
        return ResponseEntity.ok(mapOf("hasDirectPermission" to hasPermission))
    }
}
