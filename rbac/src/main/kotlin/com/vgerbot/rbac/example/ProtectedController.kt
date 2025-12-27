package com.vgerbot.rbac.example

import com.vgerbot.rbac.service.RbacService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 使用 RBAC 权限控制的示例控制器
 * 
 * 展示如何在控制器中集成权限检查
 */
@RestController
@RequestMapping("/api/example")
class ProtectedController {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    /**
     * 示例 1: 手动检查权限
     */
    @GetMapping("/manual-check")
    fun manualPermissionCheck(@RequestParam userId: Int): ResponseEntity<String> {
        // 手动检查用户是否有权限
        if (!rbacService.hasPermission(userId, "user:read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("您没有权限访问此资源")
        }
        
        return ResponseEntity.ok("访问成功：您拥有 user:read 权限")
    }
    
    /**
     * 示例 2: 使用 @PreAuthorize 注解检查权限
     * 
     * 注意：需要在 Spring Security 配置中启用方法级安全
     * @EnableGlobalMethodSecurity(prePostEnabled = true)
     */
    @GetMapping("/annotation-check")
    @PreAuthorize("@rbacChecker.hasPermission(#userId, 'user:delete')")
    fun annotationPermissionCheck(@RequestParam userId: Int): ResponseEntity<String> {
        return ResponseEntity.ok("访问成功：您拥有 user:delete 权限")
    }
    
    /**
     * 示例 3: 检查角色
     */
    @GetMapping("/role-check")
    @PreAuthorize("@rbacChecker.hasRole(#userId, 'ROLE_ADMIN')")
    fun roleCheck(@RequestParam userId: Int): ResponseEntity<String> {
        return ResponseEntity.ok("访问成功：您是管理员")
    }
    
    /**
     * 示例 4: 检查多个权限（任意一个）
     */
    @GetMapping("/any-permission")
    @PreAuthorize("@rbacChecker.hasAnyPermission(#userId, 'user:read', 'user:create')")
    fun anyPermissionCheck(@RequestParam userId: Int): ResponseEntity<String> {
        return ResponseEntity.ok("访问成功：您至少拥有 user:read 或 user:create 权限之一")
    }
    
    /**
     * 示例 5: 检查多个权限（全部）
     */
    @GetMapping("/all-permissions")
    @PreAuthorize("@rbacChecker.hasAllPermissions(#userId, 'user:read', 'user:update')")
    fun allPermissionsCheck(@RequestParam userId: Int): ResponseEntity<String> {
        return ResponseEntity.ok("访问成功：您同时拥有 user:read 和 user:update 权限")
    }
    
    /**
     * 示例 6: 复杂的权限组合
     */
    @GetMapping("/complex-check")
    fun complexPermissionCheck(@RequestParam userId: Int): ResponseEntity<Map<String, Any>> {
        val isAdmin = rbacService.hasRole(userId, "ROLE_ADMIN")
        val canRead = rbacService.hasPermission(userId, "user:read")
        val canWrite = rbacService.hasPermission(userId, "user:create")
        val canDelete = rbacService.hasPermission(userId, "user:delete")
        
        // 管理员或同时拥有读写权限的用户可以访问
        if (isAdmin || (canRead && canWrite)) {
            val result = mapOf(
                "status" to "success",
                "message" to "访问成功",
                "permissions" to mapOf(
                    "isAdmin" to isAdmin,
                    "canRead" to canRead,
                    "canWrite" to canWrite,
                    "canDelete" to canDelete
                )
            )
            return ResponseEntity.ok(result)
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "权限不足"))
    }
    
    /**
     * 示例 7: 获取当前用户权限详情
     */
    @GetMapping("/permissions-info")
    fun getPermissionsInfo(@RequestParam userId: Int): ResponseEntity<Map<String, Any>> {
        val roles = rbacService.getUserRoles(userId)
        val permissions = rbacService.getUserPermissions(userId)
        
        val result = mapOf(
            "userId" to userId,
            "roles" to roles.map { mapOf(
                "id" to it.id,
                "name" to it.name,
                "code" to it.code
            )},
            "permissions" to permissions.map { mapOf(
                "id" to it.id,
                "name" to it.name,
                "code" to it.code,
                "resource" to it.resource,
                "action" to it.action
            )}
        )
        
        return ResponseEntity.ok(result)
    }
    
    /**
     * 示例 8: 根据权限返回不同的响应
     */
    @GetMapping("/conditional-response")
    fun conditionalResponse(@RequestParam userId: Int): ResponseEntity<Map<String, Any>> {
        val isAdmin = rbacService.hasRole(userId, "ROLE_ADMIN")
        
        val data = mutableMapOf<String, Any>(
            "basicInfo" to "所有用户都能看到的基础信息"
        )
        
        // 管理员可以看到额外信息
        if (isAdmin) {
            data["adminInfo"] = "只有管理员能看到的信息"
            data["statistics"] = mapOf(
                "totalUsers" to 100,
                "activeUsers" to 80
            )
        }
        
        return ResponseEntity.ok(data)
    }
}

