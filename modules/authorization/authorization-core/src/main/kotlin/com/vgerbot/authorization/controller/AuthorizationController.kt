package com.vgerbot.authorization.controller

import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.authorization.api.PermissionRequest
import com.vgerbot.authorization.dto.*
import com.vgerbot.common.controller.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Authorization Controller
 * Provides permission checking, policy management, and role management APIs
 */
@Tag(name = "Authorization", description = "Authorization and policy management APIs")
@RestController
@RequestMapping("/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {
    
    /**
     * Check permission
     */
    @Operation(summary = "Enforce permission", description = "Check if a subject has permission to perform an action on a resource")
    @ApiResponse(responseCode = "200", description = "Permission check completed")
    @PostMapping("/enforce")
    fun enforce(
        @Parameter(description = "Permission enforcement request", required = true)
        @RequestBody request: EnforceRequest
    ): ResponseEntity<Map<String, Any>> {
        val allowed = authorizationService.enforce(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return EnforceResponse(allowed).ok()
    }
    
    /**
     * Batch check permissions
     */
    @Operation(summary = "Batch enforce permissions", description = "Check multiple permissions in a single request")
    @ApiResponse(responseCode = "200", description = "Batch permission check completed")
    @PostMapping("/enforce/batch")
    fun batchEnforce(
        @Parameter(description = "Batch permission enforcement request", required = true)
        @RequestBody request: BatchEnforceRequest
    ): ResponseEntity<Map<String, Any>> {
        val permissionRequests = request.requests.map {
            PermissionRequest(it.subject, it.resource, it.action, it.domain)
        }
        val results = authorizationService.batchEnforce(permissionRequests)
        
        val enforceResults = request.requests.zip(results).map { (req, allowed) ->
            EnforceResult(req, allowed)
        }
        
        return BatchEnforceResponse(enforceResults).ok()
    }
    
    /**
     * Add policy
     */
    @Operation(summary = "Add policy", description = "Add a new authorization policy")
    @ApiResponse(responseCode = "200", description = "Policy added successfully")
    @PostMapping("/policies")
    fun addPolicy(
        @Parameter(description = "Policy request", required = true)
        @RequestBody request: PolicyRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.addPolicy(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Remove policy
     */
    @Operation(summary = "Remove policy", description = "Remove an authorization policy")
    @ApiResponse(responseCode = "200", description = "Policy removed successfully")
    @DeleteMapping("/policies")
    fun removePolicy(
        @Parameter(description = "Policy request", required = true)
        @RequestBody request: PolicyRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.removePolicy(
            request.subject,
            request.resource,
            request.action,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Get all permissions for a subject
     */
    @Operation(summary = "Get permissions for subject", description = "Retrieve all permissions for a given subject")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions")
    @GetMapping("/permissions/subject/{subject}")
    fun getPermissionsForSubject(
        @Parameter(description = "Subject identifier", required = true)
        @PathVariable subject: String,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val permissions = authorizationService.getPermissionsForSubject(subject, domain)
        val result = permissions.map { 
            PermissionInfo(it.subject, it.resource, it.action, it.domain)
        }
        return result.ok()
    }
    
    /**
     * Get all permissions for a resource
     */
    @Operation(summary = "Get permissions for resource", description = "Retrieve all permissions for a given resource")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions")
    @GetMapping("/permissions/resource/{resource}")
    fun getPermissionsForResource(
        @Parameter(description = "Resource identifier", required = true)
        @PathVariable resource: String,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val permissions = authorizationService.getPermissionsForResource(resource, domain)
        val result = permissions.map { 
            PermissionInfo(it.subject, it.resource, it.action, it.domain)
        }
        return result.ok()
    }
    
    /**
     * Add role for user
     */
    @Operation(summary = "Add role for user", description = "Assign a role to a user")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully")
    @PostMapping("/users/roles")
    fun addRoleForUser(
        @Parameter(description = "Role assignment request", required = true)
        @RequestBody request: RoleAssignmentRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.addRoleForUser(
            request.userId,
            request.role,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Remove role from user
     */
    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    @ApiResponse(responseCode = "200", description = "Role removed successfully")
    @DeleteMapping("/users/roles")
    fun removeRoleForUser(
        @Parameter(description = "Role assignment request", required = true)
        @RequestBody request: RoleAssignmentRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.removeRoleForUser(
            request.userId,
            request.role,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Get all roles for a user
     */
    @Operation(summary = "Get roles for user", description = "Retrieve all roles assigned to a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user roles")
    @GetMapping("/users/{userId}/roles")
    fun getRolesForUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val roles = authorizationService.getRolesForUser(userId, domain)
        return UserRoleInfo(userId, roles, domain).ok()
    }
    
    /**
     * Delete all roles for a user
     */
    @Operation(summary = "Delete roles for user", description = "Remove all roles from a user")
    @ApiResponse(responseCode = "200", description = "Roles deleted successfully")
    @DeleteMapping("/users/{userId}/roles")
    fun deleteRolesForUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.deleteRolesForUser(userId, domain)
        return mapOf("success" to success).ok()
    }
    
    /**
     * Get all users with a specific role
     */
    @Operation(summary = "Get users for role", description = "Retrieve all users assigned to a specific role")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @GetMapping("/roles/{role}/users")
    fun getUsersForRole(
        @Parameter(description = "Role identifier", required = true)
        @PathVariable role: String,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val users = authorizationService.getUsersForRole(role, domain)
        return mapOf("users" to users).ok()
    }
    
    /**
     * Add role inheritance
     */
    @Operation(summary = "Add role inheritance", description = "Add a role inheritance relationship")
    @ApiResponse(responseCode = "200", description = "Role inheritance added successfully")
    @PostMapping("/roles/inheritance")
    fun addRoleInheritance(
        @Parameter(description = "Role inheritance request", required = true)
        @RequestBody request: RoleInheritanceRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.addRoleInheritance(
            request.role,
            request.parentRole,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Remove role inheritance
     */
    @Operation(summary = "Remove role inheritance", description = "Remove a role inheritance relationship")
    @ApiResponse(responseCode = "200", description = "Role inheritance removed successfully")
    @DeleteMapping("/roles/inheritance")
    fun removeRoleInheritance(
        @Parameter(description = "Role inheritance request", required = true)
        @RequestBody request: RoleInheritanceRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.removeRoleInheritance(
            request.role,
            request.parentRole,
            request.domain
        )
        return mapOf("success" to success).ok()
    }
    
    /**
     * Delete role
     */
    @Operation(summary = "Delete role", description = "Delete a role from the system")
    @ApiResponse(responseCode = "200", description = "Role deleted successfully")
    @DeleteMapping("/roles/{role}")
    fun deleteRole(
        @Parameter(description = "Role identifier", required = true)
        @PathVariable role: String,
        @Parameter(description = "Optional domain filter")
        @RequestParam(required = false) domain: String?
    ): ResponseEntity<Map<String, Any>> {
        val success = authorizationService.deleteRole(role, domain)
        return mapOf("success" to success).ok()
    }
    
    /**
     * Reload policy
     */
    @Operation(summary = "Reload policy", description = "Reload authorization policies from storage")
    @ApiResponse(responseCode = "200", description = "Policy reloaded successfully")
    @PostMapping("/reload")
    fun reloadPolicy(): ResponseEntity<Map<String, Any>> {
        authorizationService.reloadPolicy()
        return ok("策略重新加载成功")
    }
}

