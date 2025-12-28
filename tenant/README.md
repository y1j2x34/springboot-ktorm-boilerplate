# Tenant Module - 租户模块

## 概述

Tenant 模块为应用提供多租户支持，允许将租户信息无缝集成到 Spring Security 的 Principal 对象中，而无需修改现有的 `user`、`jwt-auth` 和 `rbac` 模块。

## 核心特性

- ✅ **无侵入设计**：不修改现有模块，通过装饰器模式扩展功能
- ✅ **自动注入租户信息**：在 JWT 认证后自动将租户信息注入到 Principal
- ✅ **多种获取方式**：支持从 SecurityContext、ThreadLocal 等多种方式获取租户信息
- ✅ **灵活的租户切换**：支持通过请求头指定租户（需验证用户权限）
- ✅ **ThreadLocal 上下文**：在整个请求生命周期内可访问租户信息

## 架构设计

### 核心组件

1. **TenantPrincipal** - 包装原有 UserDetails，添加租户信息
2. **TenantAuthenticationToken** - 扩展 UsernamePasswordAuthenticationToken，携带租户信息
3. **TenantAuthenticationFilter** - 在 JWT 认证后注入租户信息的过滤器
4. **TenantContextHolder** - ThreadLocal 上下文持有者
5. **TenantUtils** - 便捷的工具类，用于获取当前租户信息

### 工作流程

```
用户请求
    ↓
JwtRequestFilter (JWT 认证)
    ↓
TenantAuthenticationFilter (注入租户信息)
    ↓
    1. 从 SecurityContext 获取已认证用户
    2. 根据用户名查询用户 ID
    3. 查询用户的租户信息（默认租户或请求头指定）
    4. 包装 Principal 为 TenantPrincipal
    5. 创建 TenantAuthenticationToken
    6. 替换 SecurityContext 中的 Authentication
    7. 设置 ThreadLocal 上下文
    ↓
业务逻辑处理（可随时获取租户信息）
    ↓
请求完成（自动清理 ThreadLocal）
```

## 数据库设计

### 租户表 (tenant)

```sql
CREATE TABLE `tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '租户代码',
    `name` VARCHAR(100) NOT NULL COMMENT '租户名称',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
```

### 用户租户关联表 (user_tenant)

```sql
CREATE TABLE `user_tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `tenant_id` INT NOT NULL COMMENT '租户ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tenant` (`user_id`, `tenant_id`)
);
```

## 使用指南

### 1. 获取当前租户信息

#### 方式一：使用 TenantUtils（推荐）

```kotlin
import com.vgerbot.tenant.utils.TenantUtils

@RestController
class MyController {
    @GetMapping("/my-data")
    fun getMyData(): ResponseEntity<*> {
        val tenantId = TenantUtils.getCurrentTenantId()
        val tenantCode = TenantUtils.getCurrentTenantCode()
        
        if (tenantId != null) {
            // 使用租户 ID 过滤数据
            return ResponseEntity.ok("Current tenant: $tenantCode")
        }
        
        return ResponseEntity.ok("No tenant context")
    }
}
```

#### 方式二：从 SecurityContext 获取

```kotlin
import com.vgerbot.tenant.security.TenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

val authentication = SecurityContextHolder.getContext().authentication
if (authentication is TenantAuthenticationToken) {
    val tenantId = authentication.tenantId
    val tenantCode = authentication.tenantCode
    val tenantName = authentication.tenantName
}
```

#### 方式三：从 Principal 获取

```kotlin
import com.vgerbot.tenant.security.TenantPrincipal
import java.security.Principal

@GetMapping("/info")
fun getInfo(principal: Principal): ResponseEntity<*> {
    if (principal is TenantPrincipal) {
        return ResponseEntity.ok(mapOf(
            "username" to principal.username,
            "tenantId" to principal.tenantId,
            "tenantCode" to principal.tenantCode
        ))
    }
    return ResponseEntity.ok("No tenant info")
}
```

#### 方式四：使用 @AuthenticationPrincipal

```kotlin
import com.vgerbot.tenant.security.TenantPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal

@GetMapping("/profile")
fun getProfile(@AuthenticationPrincipal tenantPrincipal: TenantPrincipal): ResponseEntity<*> {
    return ResponseEntity.ok(mapOf(
        "username" to tenantPrincipal.username,
        "tenantId" to tenantPrincipal.tenantId,
        "tenantCode" to tenantPrincipal.tenantCode,
        "tenantName" to tenantPrincipal.tenantName
    ))
}
```

### 2. 在 Service 层使用租户信息

```kotlin
import com.vgerbot.tenant.context.TenantContextHolder

@Service
class ProductService {
    fun getProducts(): List<Product> {
        val tenantId = TenantContextHolder.getTenantId()
        
        // 使用租户 ID 过滤数据
        return if (tenantId != null) {
            productDao.findByTenantId(tenantId)
        } else {
            emptyList()
        }
    }
}
```

### 3. 通过请求头切换租户

用户可以在请求头中指定租户 ID 来切换到其他租户（前提是用户属于该租户）：

```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     https://api.example.com/api/data
```

租户切换策略：
1. 如果请求头中指定了 `X-Tenant-Id`，且用户属于该租户，则使用指定的租户
2. 否则使用用户的默认租户
3. 如果用户没有任何租户，租户信息为 null

### 4. 数据隔离最佳实践

#### 在 DAO 层自动过滤

```kotlin
import com.vgerbot.tenant.context.TenantContextHolder
import org.ktorm.dsl.eq
import org.ktorm.dsl.and

@Repository
class ProductDao {
    @Autowired
    private lateinit var database: Database
    
    fun findAll(): List<Product> {
        val tenantId = TenantContextHolder.getTenantId()
        
        return if (tenantId != null) {
            database.products
                .filter { it.tenantId eq tenantId }
                .toList()
        } else {
            emptyList()
        }
    }
}
```

#### 使用 Ktorm 扩展

```kotlin
// 创建一个扩展函数来自动添加租户过滤
fun <E : Entity<E>, T : BaseTable<E>> EntitySequence<E, T>.filterByTenant(): EntitySequence<E, T> {
    val tenantId = TenantContextHolder.getTenantId() ?: return this
    
    return this.filter { 
        (it as? TenantAwareTable)?.tenantId eq tenantId 
    }
}

// 使用
database.products.filterByTenant().toList()
```

## 集成说明

### 已完成的集成

tenant 模块已经集成到 app 模块中，通过以下方式：

1. **依赖配置**：`app/build.gradle.kts` 已添加 `implementation(project(":tenant"))`
2. **过滤器注册**：`AppSecurityConfiguration` 已将 `TenantAuthenticationFilter` 注册到 Spring Security 过滤器链
3. **JwtConfiguration 排除**：在 `Application.kt` 中排除了 `jwt-auth` 模块的默认配置，使用 app 层的统一配置

### 过滤器顺序

```
JwtRequestFilter (JWT 认证)
    ↓
TenantAuthenticationFilter (注入租户信息)
    ↓
业务处理
```

## API 示例

### 获取当前租户信息

```bash
GET /api/tenant/current
Authorization: Bearer <your-jwt-token>

Response:
{
    "fromToken": {
        "tenantId": 1,
        "tenantCode": "tenant_demo",
        "tenantName": "演示租户"
    },
    "fromPrincipal": {
        "tenantId": 1,
        "tenantCode": "tenant_demo",
        "tenantName": "演示租户",
        "username": "user123"
    },
    "fromUtils": {
        "tenantId": 1,
        "tenantCode": "tenant_demo",
        "tenantName": "演示租户"
    }
}
```

## 测试

### 运行数据库初始化

```bash
# 确保数据库正在运行
cd database
docker-compose up -d

# 初始化脚本会自动创建租户相关的表和示例数据
mysql -h localhost -u root -p spring-boot-kt < init.sql
```

### 测试流程

1. 登录获取 JWT Token
2. 使用 Token 访问 `/api/tenant/current` 查看租户信息
3. 尝试使用 `X-Tenant-Id` 头切换租户（如果用户有多个租户）

## 注意事项

1. **性能考虑**：TenantAuthenticationFilter 会在每个请求中查询数据库以获取租户信息，建议添加缓存
2. **线程安全**：TenantContextHolder 使用 ThreadLocal，在异步场景下需要注意传递
3. **清理**：过滤器会在请求结束时自动清理 ThreadLocal，避免内存泄漏
4. **租户验证**：切换租户时会验证用户是否属于目标租户，确保安全性

## 扩展建议

### 1. 添加租户缓存

```kotlin
@Service
class TenantService {
    @Autowired
    private lateinit var cacheManager: CacheManager
    
    @Cacheable("tenant", key = "#userId")
    fun getDefaultTenantForUser(userId: Int): Tenant? {
        // ... 查询逻辑
    }
}
```

### 2. 支持动态数据源切换

如果每个租户使用独立的数据库，可以结合 AbstractRoutingDataSource 实现：

```kotlin
class TenantRoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any? {
        return TenantContextHolder.getTenantCode()
    }
}
```

### 3. 添加租户级别的 RBAC

可以在 rbac 模块中添加租户维度，实现更细粒度的权限控制。

## 故障排查

### 租户信息为 null

1. 检查用户是否在 `user_tenant` 表中有关联记录
2. 确认 `TenantAuthenticationFilter` 是否正确注册
3. 查看日志，确认过滤器是否执行

### 无法切换租户

1. 确认请求头 `X-Tenant-Id` 是否正确设置
2. 检查用户是否属于目标租户
3. 查看过滤器日志

## 许可

MIT License

