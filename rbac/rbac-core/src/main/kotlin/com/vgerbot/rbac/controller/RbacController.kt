package com.vgerbot.rbac.controller

import com.vgerbot.rbac.dto.*
import com.vgerbot.rbac.service.RbacService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rbac")
class RbacController {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    // 用户角色管理
    @PostMapping("/users/roles")
    fun assignRoleToUser(@RequestBody dto: AssignRoleToUserDto): ResponseEntity<Any> {
        val assigned = rbacService.assignRoleToUser(dto.userId, dto.roleId)
        return if (assigned) {
            ResponseEntity.ok(mapOf("message" to "Role assigned to user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Role already assigned to user"))
        }
    }
    
    @DeleteMapping("/users/roles")
    fun removeRoleFromUser(@RequestBody dto: RemoveRoleFromUserDto): ResponseEntity<Any> {
        val removed = rbacService.removeRoleFromUser(dto.userId, dto.roleId)
        return if (removed) {
            ResponseEntity.ok(mapOf("message" to "Role removed from user successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "User role assignment not found"))
        }
    }
    
    @GetMapping("/users/{userId}/roles")
    fun getUserRoles(@PathVariable userId: Int): ResponseEntity<List<RoleDto>> {
        val roles = rbacService.getUserRoles(userId)
        return ResponseEntity.ok(roles)
    }
    
    @GetMapping("/users/{userId}/permissions")
    fun getUserPermissions(@PathVariable userId: Int): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacService.getUserPermissions(userId)
        return ResponseEntity.ok(permissions)
    }
    
    // 角色权限管理
    @PostMapping("/roles/permissions")
    fun assignPermissionToRole(@RequestBody dto: AssignPermissionToRoleDto): ResponseEntity<Any> {
        val assigned = rbacService.assignPermissionToRole(dto.roleId, dto.permissionId)
        return if (assigned) {
            ResponseEntity.ok(mapOf("message" to "Permission assigned to role successfully"))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Permission already assigned to role"))
        }
    }
    
    @DeleteMapping("/roles/permissions")
    fun removePermissionFromRole(@RequestBody dto: RemovePermissionFromRoleDto): ResponseEntity<Any> {
        val removed = rbacService.removePermissionFromRole(dto.roleId, dto.permissionId)
        return if (removed) {
            ResponseEntity.ok(mapOf("message" to "Permission removed from role successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Role permission assignment not found"))
        }
    }
    
    @GetMapping("/roles/{roleId}/permissions")
    fun getRolePermissions(@PathVariable roleId: Int): ResponseEntity<List<PermissionDto>> {
        val permissions = rbacService.getRolePermissions(roleId)
        return ResponseEntity.ok(permissions)
    }
    
    // 权限检查
    @GetMapping("/users/{userId}/has-permission/{permissionCode}")
    fun hasPermission(@PathVariable userId: Int, @PathVariable permissionCode: String): ResponseEntity<Map<String, Boolean>> {
        val hasPermission = rbacService.hasPermission(userId, permissionCode)
        return ResponseEntity.ok(mapOf("hasPermission" to hasPermission))
    }
    
    @GetMapping("/users/{userId}/has-role/{roleCode}")
    fun hasRole(@PathVariable userId: Int, @PathVariable roleCode: String): ResponseEntity<Map<String, Boolean>> {
        val hasRole = rbacService.hasRole(userId, roleCode)
        return ResponseEntity.ok(mapOf("hasRole" to hasRole))
    }
}

