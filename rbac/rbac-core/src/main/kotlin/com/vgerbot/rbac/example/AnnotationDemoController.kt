package com.vgerbot.rbac.example

import com.vgerbot.common.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 权限注解使用示例控制器
 * 
 * 展示如何使用权限注解来控制访问权限
 */
@RestController
@RequestMapping("/api/demo")
class AnnotationDemoController {
    
    /**
     * 示例 1: 使用 resource 和 action 指定权限
     */
    @GetMapping("/users")
    @RequiresPermission(resource = "user", action = "read")
    fun listUsers(): ResponseEntity<*> {
        // 需要 user:read 权限
        return ResponseEntity.ok(mapOf(
            "message" to "用户列表",
            "requiredPermission" to "user:read"
        ))
    }
    
    /**
     * 示例 2: 使用完整的权限码
     */
    @PostMapping("/users")
    @RequiresPermission(value = "user:create")
    fun createUser(@RequestBody data: Map<String, Any>): ResponseEntity<*> {
        // 需要 user:create 权限
        return ResponseEntity.ok(mapOf(
            "message" to "用户创建成功",
            "requiredPermission" to "user:create"
        ))
    }
    
    /**
     * 示例 3: 需要多个权限（任意一个）
     */
    @PutMapping("/users/{id}")
    @RequiresPermissions(
        RequiresPermission(resource = "user", action = "update"),
        RequiresPermission(resource = "user", action = "admin"),
        requireAll = false  // 拥有 user:update 或 user:admin 任意一个即可
    )
    fun updateUser(@PathVariable id: Int, @RequestBody data: Map<String, Any>): ResponseEntity<*> {
        return ResponseEntity.ok(mapOf(
            "message" to "用户更新成功",
            "userId" to id,
            "requiredPermissions" to listOf("user:update OR user:admin")
        ))
    }
    
    /**
     * 示例 4: 需要多个权限（全部）
     */
    @DeleteMapping("/users/{id}")
    @RequiresPermissions(
        RequiresPermission(resource = "user", action = "delete"),
        RequiresPermission(resource = "user", action = "admin"),
        requireAll = true  // 必须同时拥有两个权限
    )
    fun deleteUser(@PathVariable id: Int): ResponseEntity<*> {
        return ResponseEntity.ok(mapOf(
            "message" to "用户删除成功",
            "userId" to id,
            "requiredPermissions" to listOf("user:delete AND user:admin")
        ))
    }
    
    /**
     * 示例 5: 使用角色注解
     */
    @GetMapping("/admin/dashboard")
    @RequiresRole("ROLE_ADMIN")
    fun adminDashboard(): ResponseEntity<*> {
        // 只有管理员可以访问
        return ResponseEntity.ok(mapOf(
            "message" to "管理员仪表盘",
            "requiredRole" to "ROLE_ADMIN"
        ))
    }
    
    /**
     * 示例 6: 需要多个角色（任意一个）
     */
    @GetMapping("/protected/resource")
    @RequiresRole("ROLE_ADMIN", "ROLE_MANAGER", requireAll = false)
    fun protectedResource(): ResponseEntity<*> {
        // 管理员或经理都可以访问
        return ResponseEntity.ok(mapOf(
            "message" to "受保护的资源",
            "requiredRoles" to listOf("ROLE_ADMIN OR ROLE_MANAGER")
        ))
    }
    
    /**
     * 示例 7: 公开访问（无需权限）
     */
    @GetMapping("/public/info")
    @PublicAccess
    fun publicInfo(): ResponseEntity<*> {
        // 所有人都可以访问，包括未登录用户
        return ResponseEntity.ok(mapOf(
            "message" to "公开信息",
            "access" to "public"
        ))
    }
    
    /**
     * 示例 8: 组合使用角色和权限
     */
    @GetMapping("/sensitive/data")
    @RequiresRole("ROLE_ADMIN")
    @RequiresPermission(resource = "sensitive", action = "read")
    fun sensitiveData(): ResponseEntity<*> {
        // 必须是管理员，并且拥有 sensitive:read 权限
        return ResponseEntity.ok(mapOf(
            "message" to "敏感数据",
            "required" to "ROLE_ADMIN AND sensitive:read"
        ))
    }
    
    /**
     * 示例 9: 类级别的权限注解
     * 
     * 下面的示例展示了如何在类级别应用权限注解
     */
    @RestController
    @RequestMapping("/api/products")
    @RequiresPermission(resource = "product", action = "read")  // 类级别：所有方法都需要此权限
    class ProductController {
        
        @GetMapping
        fun listProducts(): ResponseEntity<*> {
            // 继承类级别的 product:read 权限
            return ResponseEntity.ok(listOf("产品A", "产品B"))
        }
        
        @GetMapping("/{id}")
        fun getProduct(@PathVariable id: Int): ResponseEntity<*> {
            // 继承类级别的 product:read 权限
            return ResponseEntity.ok(mapOf("id" to id, "name" to "产品$id"))
        }
        
        @PostMapping
        @RequiresPermission(resource = "product", action = "create")  // 方法级别权限会覆盖类级别
        fun createProduct(@RequestBody data: Map<String, Any>): ResponseEntity<*> {
            // 需要 product:create 权限（方法级别优先）
            return ResponseEntity.ok(mapOf("message" to "产品创建成功"))
        }
    }
}

/**
 * 测试端点控制器（用于测试权限系统）
 */
@RestController
@RequestMapping("/api/test/permission")
class PermissionTestController {
    
    /**
     * 测试无权限访问（应该被拦截）
     */
    @GetMapping("/no-permission")
    @RequiresPermission(resource = "test", action = "forbidden")
    fun noPermission(): ResponseEntity<*> {
        return ResponseEntity.ok("如果你看到这个消息，说明权限拦截失败了")
    }
    
    /**
     * 测试管理员权限
     */
    @GetMapping("/admin-only")
    @RequiresRole("ROLE_ADMIN")
    fun adminOnly(): ResponseEntity<*> {
        return ResponseEntity.ok("欢迎，管理员！")
    }
    
    /**
     * 测试公开访问
     */
    @GetMapping("/public")
    @PublicAccess
    fun publicEndpoint(): ResponseEntity<*> {
        return ResponseEntity.ok(mapOf(
            "message" to "这是公开端点",
            "timestamp" to System.currentTimeMillis()
        ))
    }
}

