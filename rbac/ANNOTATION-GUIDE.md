# 基于注解的权限控制系统

## 概述

本系统提供了一套基于注解的权限控制机制，可以方便地在 Controller 方法上标记所需的权限，系统会自动拦截请求并检查用户是否拥有相应的权限。

## 核心注解

### 1. @RequiresPermission - 权限注解

标记方法需要的操作权限。

**参数说明**：
- `value`: 完整的权限码，如 `"user:delete"`
- `resource`: 资源类型，如 `"user"`, `"order"`
- `action`: 操作类型，如 `"read"`, `"create"`, `"update"`, `"delete"`
- `requireAll`: 多权限时是否要求全部拥有（默认 false）

**使用方式**：

```kotlin
// 方式1: 使用 resource 和 action
@RequiresPermission(resource = "user", action = "delete")
@DeleteMapping("/{id}")
fun deleteUser(@PathVariable id: Int): ResponseEntity<*>

// 方式2: 使用完整的权限码
@RequiresPermission(value = "user:delete")
@DeleteMapping("/{id}")
fun deleteUser(@PathVariable id: Int): ResponseEntity<*>
```

### 2. @RequiresPermissions - 多权限注解

标记方法需要多个权限。

**使用示例**：

```kotlin
// 需要任意一个权限
@RequiresPermissions(
    RequiresPermission(resource = "user", action = "update"),
    RequiresPermission(resource = "user", action = "admin"),
    requireAll = false  // 拥有任意一个即可
)
@PutMapping("/{id}")
fun updateUser(@PathVariable id: Int): ResponseEntity<*>

// 需要所有权限
@RequiresPermissions(
    RequiresPermission(resource = "user", action = "delete"),
    RequiresPermission(resource = "user", action = "admin"),
    requireAll = true  // 必须同时拥有
)
@DeleteMapping("/{id}")
fun deleteUser(@PathVariable id: Int): ResponseEntity<*>
```

### 3. @RequiresRole - 角色注解

标记方法需要的角色。

**使用示例**：

```kotlin
// 需要单个角色
@RequiresRole("ROLE_ADMIN")
@GetMapping("/admin/dashboard")
fun adminDashboard(): ResponseEntity<*>

// 需要多个角色（任意一个）
@RequiresRole("ROLE_ADMIN", "ROLE_MANAGER", requireAll = false)
@GetMapping("/protected/resource")
fun protectedResource(): ResponseEntity<*>

// 需要多个角色（全部）
@RequiresRole("ROLE_ADMIN", "ROLE_SUPER_ADMIN", requireAll = true)
@GetMapping("/super/protected")
fun superProtected(): ResponseEntity<*>
```

### 4. @PublicAccess - 公开访问注解

标记方法无需权限验证，允许所有用户（包括未登录用户）访问。

**使用示例**：

```kotlin
@PublicAccess
@GetMapping("/public/info")
fun publicInfo(): ResponseEntity<*> {
    return ResponseEntity.ok("所有人都可以访问")
}
```

## 类级别注解

注解可以应用在类级别，作用于该类的所有方法。方法级别的注解会覆盖类级别的注解。

**示例**：

```kotlin
@RestController
@RequestMapping("/api/products")
@RequiresPermission(resource = "product", action = "read")  // 类级别：所有方法的默认权限
class ProductController {
    
    @GetMapping
    fun listProducts(): ResponseEntity<*> {
        // 继承类级别的 product:read 权限
    }
    
    @PostMapping
    @RequiresPermission(resource = "product", action = "create")  // 方法级别会覆盖类级别
    fun createProduct(): ResponseEntity<*> {
        // 需要 product:create 权限
    }
    
    @GetMapping("/public")
    @PublicAccess  // 公开访问会覆盖类级别的权限要求
    fun publicProducts(): ResponseEntity<*> {
        // 所有人都可以访问
    }
}
```

## 工作原理

### 1. 拦截流程

```
客户端请求
    ↓
RbacInterceptor 拦截
    ↓
检查是否标记 @PublicAccess → 是 → 允许访问
    ↓ 否
获取当前用户ID（从Token/Session等）
    ↓
用户未登录 → 返回 401 Unauthorized
    ↓ 已登录
检查 @RequiresRole → 无角色 → 返回 403 Forbidden
    ↓ 有角色
检查 @RequiresPermission(s) → 无权限 → 返回 403 Forbidden
    ↓ 有权限
执行 Controller 方法
```

### 2. 权限检查逻辑

```kotlin
// 单个权限
rbacService.hasPermission(userId, "user:delete")

// 多个权限（任意一个）
permissionCodes.any { rbacService.hasPermission(userId, it) }

// 多个权限（全部）
permissionCodes.all { rbacService.hasPermission(userId, it) }

// 角色检查
rbacService.hasRole(userId, "ROLE_ADMIN")
```

## 配置说明

### 1. 基本配置（使用 RbacInterceptor）

如果你的项目使用简单的身份验证（如通过请求参数或请求头传递 userId）：

```kotlin
// rbac/src/main/kotlin/com/vgerbot/rbac/configuration/RbacWebConfiguration.kt
@Configuration
class RbacWebConfiguration : WebMvcConfigurer {
    
    @Autowired
    lateinit var rbacInterceptor: RbacInterceptor
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rbacInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/api/auth/**", "/public/**")
    }
}
```

### 2. JWT 集成配置（使用 JwtRbacInterceptor）

如果你的项目使用 JWT 进行身份验证：

```kotlin
@Configuration
class RbacWebConfiguration : WebMvcConfigurer {
    
    @Autowired
    lateinit var jwtRbacInterceptor: JwtRbacInterceptor
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(jwtRbacInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/api/auth/**", "/public/**")
    }
}
```

### 3. 自定义用户ID获取方式

在 `RbacInterceptor` 的 `getCurrentUserId()` 方法中自定义获取逻辑：

```kotlin
private fun getCurrentUserId(request: HttpServletRequest): Int? {
    // 方式1: 从请求头获取
    request.getHeader("X-User-Id")?.toIntOrNull()?.let {
        return it
    }
    
    // 方式2: 从 JWT Token 中解析
    val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
    if (token != null) {
        return jwtTokenUtils.getUserIdFromToken(token)
    }
    
    // 方式3: 从 Session 获取
    val session = request.getSession(false)
    return session?.getAttribute("userId") as? Int
    
    // 方式4: 从 Spring Security Context 获取
    val authentication = SecurityContextHolder.getContext().authentication
    if (authentication != null && authentication.isAuthenticated) {
        return (authentication.principal as? CustomUserDetails)?.userId
    }
    
    return null
}
```

## 使用示例

### 示例 1: 用户管理 API

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {
    
    @GetMapping
    @RequiresPermission(resource = "user", action = "read")
    fun listUsers(): ResponseEntity<*> {
        // 需要 user:read 权限
        return ResponseEntity.ok(userService.findAll())
    }
    
    @PostMapping
    @RequiresPermission(resource = "user", action = "create")
    fun createUser(@RequestBody user: User): ResponseEntity<*> {
        // 需要 user:create 权限
        return ResponseEntity.ok(userService.create(user))
    }
    
    @PutMapping("/{id}")
    @RequiresPermission(resource = "user", action = "update")
    fun updateUser(@PathVariable id: Int, @RequestBody user: User): ResponseEntity<*> {
        // 需要 user:update 权限
        return ResponseEntity.ok(userService.update(id, user))
    }
    
    @DeleteMapping("/{id}")
    @RequiresRole("ROLE_ADMIN")  // 只有管理员可以删除用户
    @RequiresPermission(resource = "user", action = "delete")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<*> {
        // 需要 ROLE_ADMIN 角色 AND user:delete 权限
        userService.delete(id)
        return ResponseEntity.ok(mapOf("message" to "删除成功"))
    }
}
```

### 示例 2: 订单管理 API

```kotlin
@RestController
@RequestMapping("/api/orders")
@RequiresPermission(resource = "order", action = "read")  // 类级别：默认需要读取权限
class OrderController {
    
    @GetMapping
    fun listOrders(): ResponseEntity<*> {
        // 继承类级别的 order:read 权限
        return ResponseEntity.ok(orderService.findAll())
    }
    
    @PostMapping
    @RequiresPermission(resource = "order", action = "create")  // 覆盖类级别
    fun createOrder(@RequestBody order: Order): ResponseEntity<*> {
        // 需要 order:create 权限
        return ResponseEntity.ok(orderService.create(order))
    }
    
    @PostMapping("/{id}/approve")
    @RequiresPermissions(
        RequiresPermission(resource = "order", action = "approve"),
        RequiresPermission(resource = "order", action = "admin"),
        requireAll = false  // 拥有审批权限或管理员权限均可
    )
    fun approveOrder(@PathVariable id: Int): ResponseEntity<*> {
        // 需要 order:approve 或 order:admin 权限
        orderService.approve(id)
        return ResponseEntity.ok(mapOf("message" to "订单已审批"))
    }
}
```

### 示例 3: 混合使用角色和权限

```kotlin
@RestController
@RequestMapping("/api/sensitive")
class SensitiveController {
    
    @GetMapping("/data")
    @RequiresRole("ROLE_ADMIN", "ROLE_AUDITOR", requireAll = false)
    @RequiresPermission(resource = "sensitive", action = "read")
    fun getSensitiveData(): ResponseEntity<*> {
        // 需要：(ROLE_ADMIN 或 ROLE_AUDITOR) AND sensitive:read
        return ResponseEntity.ok(sensitiveService.getData())
    }
    
    @PostMapping("/export")
    @RequiresRole("ROLE_ADMIN")
    @RequiresPermissions(
        RequiresPermission(resource = "sensitive", action = "read"),
        RequiresPermission(resource = "sensitive", action = "export"),
        requireAll = true
    )
    fun exportData(): ResponseEntity<*> {
        // 需要：ROLE_ADMIN AND sensitive:read AND sensitive:export
        return ResponseEntity.ok(sensitiveService.export())
    }
}
```

## API 测试

### 测试步骤

1. **获取 Token（如果使用 JWT）**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```

2. **访问需要权限的端点**

```bash
# 使用 JWT Token
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 或者使用 X-User-Id 请求头（测试模式）
curl http://localhost:8080/api/users \
  -H "X-User-Id: 1"

# 或者使用请求参数（测试模式）
curl "http://localhost:8080/api/users?userId=1"
```

3. **测试权限拦截**

```bash
# 访问需要管理员权限的端点（应该返回 403 如果不是管理员）
curl http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 访问公开端点（应该成功）
curl http://localhost:8080/api/public/info
```

### 预期响应

```json
// 401 Unauthorized - 未登录
{
  "timestamp": "2025-12-27T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "请先登录",
  "path": "/api/users"
}

// 403 Forbidden - 无权限
{
  "timestamp": "2025-12-27T10:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "权限不足",
  "path": "/api/users/1"
}

// 200 OK - 有权限
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com"
}
```

## 常见问题

### Q1: 如何调试权限拦截？

在 `application.yml` 中启用 DEBUG 日志：

```yaml
logging:
  level:
    com.vgerbot.rbac.interceptor: DEBUG
```

### Q2: 如何跳过某些路径的权限检查？

在 `RbacWebConfiguration` 中配置 `excludePathPatterns`：

```kotlin
registry.addInterceptor(rbacInterceptor)
    .addPathPatterns("/**")
    .excludePathPatterns(
        "/api/auth/**",
        "/public/**",
        "/health",
        "/actuator/**"
    )
```

### Q3: 如何实现动态权限？

权限数据存储在数据库中，可以随时添加、修改、删除：

```kotlin
// 添加新权限
permissionService.createPermission(CreatePermissionDto(
    name = "导出报表",
    code = "report:export",
    resource = "report",
    action = "export"
))

// 为角色分配权限
rbacService.assignPermissionToRole(roleId, permissionId)

// 为用户分配角色
rbacService.assignRoleToUser(userId, roleId)
```

### Q4: 如何实现前端权限控制？

提供一个 API 获取当前用户的权限列表：

```kotlin
@GetMapping("/api/current-user/permissions")
@PublicAccess  // 或根据需要设置权限
fun getCurrentUserPermissions(@RequestParam userId: Int): ResponseEntity<*> {
    val permissions = rbacService.getUserPermissions(userId)
    val roles = rbacService.getUserRoles(userId)
    
    return ResponseEntity.ok(mapOf(
        "permissions" to permissions.map { it.code },
        "roles" to roles.map { it.code }
    ))
}
```

前端根据权限列表显示/隐藏按钮或菜单。

## 性能优化

### 1. 添加权限缓存

```kotlin
@Service
@CacheConfig(cacheNames = ["userPermissions"])
class CachedRbacService {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    @Cacheable(key = "#userId")
    fun getUserPermissions(userId: Int): List<Permission> {
        return rbacService.getUserPermissions(userId)
    }
    
    @CacheEvict(key = "#userId")
    fun clearCache(userId: Int) {
        // 当用户权限变更时清除缓存
    }
}
```

### 2. 使用 Redis 缓存

```kotlin
@Autowired
lateinit var redisTemplate: RedisTemplate<String, Any>

fun getUserPermissions(userId: Int): List<String> {
    val cacheKey = "user:$userId:permissions"
    
    // 先从缓存获取
    val cached = redisTemplate.opsForValue().get(cacheKey)
    if (cached != null) {
        return cached as List<String>
    }
    
    // 从数据库查询
    val permissions = rbacService.getUserPermissions(userId)
        .map { it.code }
    
    // 存入缓存（5分钟过期）
    redisTemplate.opsForValue().set(cacheKey, permissions, 5, TimeUnit.MINUTES)
    
    return permissions
}
```

## 总结

基于注解的权限控制系统具有以下优势：

1. ✅ **声明式编程** - 权限要求清晰可见
2. ✅ **易于维护** - 权限逻辑集中管理
3. ✅ **灵活配置** - 支持多种组合方式
4. ✅ **自动拦截** - 无需手动编写权限检查代码
5. ✅ **类型安全** - 编译时检查注解参数
6. ✅ **可扩展** - 易于添加新的注解和拦截逻辑

配合 RBAC 模块的数据库管理，可以实现完整的动态权限控制系统。

