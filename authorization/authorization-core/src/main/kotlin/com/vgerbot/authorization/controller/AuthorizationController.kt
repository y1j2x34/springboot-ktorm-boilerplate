package com.vgerbot.authorization.controller

import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.authorization.api.PermissionRequest
import com.vgerbot.authorization.dto.*
import org.springframework.web.bind.annotation.*

/**
 * 授权管理控制器
 * 提供权限检查、策略管理、角色管理等 API
 */
@RestController
@RequestMapping("/api/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {
    
    /**
     * 检查权限
     */
    @PostMapping("/enforce")
    fun enforce(@RequestBody request: EnforceRequest): EnforceResponse {
        val allowed = authorizationService.enforce(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return EnforceResponse(allowed)
    }
    
    /**
     * 批量检查权限
     */
    @PostMapping("/enforce/batch")
    fun batchEnforce(@RequestBody request: BatchEnforceRequest): BatchEnforceResponse {
        val permissionRequests = request.requests.map {
            PermissionRequest(it.subject, it.resource, it.action, it.domain)
        }
        val results = authorizationService.batchEnforce(permissionRequests)
        
        val enforceResults = request.requests.zip(results).map { (req, allowed) ->
            EnforceResult(req, allowed)
        }
        
        return BatchEnforceResponse(enforceResults)
    }
    
    /**
     * 添加策略
     */
    @PostMapping("/policies")
    fun addPolicy(@RequestBody request: PolicyRequest): Map<String, Boolean> {
        val success = authorizationService.addPolicy(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 删除策略
     */
    @DeleteMapping("/policies")
    fun removePolicy(@RequestBody request: PolicyRequest): Map<String, Boolean> {
        val success = authorizationService.removePolicy(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 获取主体的所有权限
     */
    @GetMapping("/permissions/subject/{subject}")
    fun getPermissionsForSubject(
        @PathVariable subject: String,
        @RequestParam(required = false) domain: String?
    ): List<PermissionInfo> {
        val permissions = authorizationService.getPermissionsForSubject(subject, domain)
        return permissions.map { 
            PermissionInfo(it.subject, it.resource, it.action, it.domain)
        }
    }
    
    /**
     * 获取资源的所有权限
     */
    @GetMapping("/permissions/resource/{resource}")
    fun getPermissionsForResource(
        @PathVariable resource: String,
        @RequestParam(required = false) domain: String?
    ): List<PermissionInfo> {
        val permissions = authorizationService.getPermissionsForResource(resource, domain)
        return permissions.map { 
            PermissionInfo(it.subject, it.resource, it.action, it.domain)
        }
    }
    
    /**
     * 为用户添加角色
     */
    @PostMapping("/users/roles")
    fun addRoleForUser(@RequestBody request: RoleAssignmentRequest): Map<String, Boolean> {
        val success = authorizationService.addRoleForUser(
            request.userId,
            request.role,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 移除用户的角色
     */
    @DeleteMapping("/users/roles")
    fun removeRoleForUser(@RequestBody request: RoleAssignmentRequest): Map<String, Boolean> {
        val success = authorizationService.removeRoleForUser(
            request.userId,
            request.role,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 获取用户的所有角色
     */
    @GetMapping("/users/{userId}/roles")
    fun getRolesForUser(
        @PathVariable userId: Int,
        @RequestParam(required = false) domain: String?
    ): UserRoleInfo {
        val roles = authorizationService.getRolesForUser(userId, domain)
        return UserRoleInfo(userId, roles, domain)
    }
    
    /**
     * 删除用户的所有角色
     */
    @DeleteMapping("/users/{userId}/roles")
    fun deleteRolesForUser(
        @PathVariable userId: Int,
        @RequestParam(required = false) domain: String?
    ): Map<String, Boolean> {
        val success = authorizationService.deleteRolesForUser(userId, domain)
        return mapOf("success" to success)
    }
    
    /**
     * 获取拥有指定角色的所有用户
     */
    @GetMapping("/roles/{role}/users")
    fun getUsersForRole(
        @PathVariable role: String,
        @RequestParam(required = false) domain: String?
    ): Map<String, List<Int>> {
        val users = authorizationService.getUsersForRole(role, domain)
        return mapOf("users" to users)
    }
    
    /**
     * 添加角色继承关系
     */
    @PostMapping("/roles/inheritance")
    fun addRoleInheritance(@RequestBody request: RoleInheritanceRequest): Map<String, Boolean> {
        val success = authorizationService.addRoleInheritance(
            request.role,
            request.parentRole,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 移除角色继承关系
     */
    @DeleteMapping("/roles/inheritance")
    fun removeRoleInheritance(@RequestBody request: RoleInheritanceRequest): Map<String, Boolean> {
        val success = authorizationService.removeRoleInheritance(
            request.role,
            request.parentRole,
            request.domain
        )
        return mapOf("success" to success)
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{role}")
    fun deleteRole(
        @PathVariable role: String,
        @RequestParam(required = false) domain: String?
    ): Map<String, Boolean> {
        val success = authorizationService.deleteRole(role, domain)
        return mapOf("success" to success)
    }
    
    /**
     * 重新加载策略
     */
    @PostMapping("/reload")
    fun reloadPolicy(): Map<String, String> {
        authorizationService.reloadPolicy()
        return mapOf("message" to "Policy reloaded successfully")
    }
}

