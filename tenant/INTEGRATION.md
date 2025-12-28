# Tenant 模块集成指南

## 概述

Tenant 模块已成功实现并集成到项目中，可以在不修改 `user`、`jwt-auth`、`rbac` 模块的情况下，将租户信息注入到 Spring Security 的 Principal 对象中。

## 核心实现策略

### 1. 装饰器模式
- **TenantPrincipal**：包装原有的 UserDetails，添加租户信息字段
- **TenantAuthenticationToken**：扩展 UsernamePasswordAuthenticationToken，携带租户信息

### 2. 过滤器链扩展
- **TenantAuthenticationFilter**：在 JwtRequestFilter 之后执行，将认证后的用户信息增强为包含租户信息的对象

### 3. 上下文管理
- **TenantContextHolder**：使用 ThreadLocal 存储租户信息，在整个请求生命周期内可访问

## 文件结构

```
tenant/
├── build.gradle.kts                    # 模块依赖配置
├── README.md                           # 详细使用文档
└── src/main/kotlin/com/vgerbot/tenant/
    ├── model/
    │   ├── Tenant.kt                   # 租户实体
    │   └── UserTenant.kt               # 用户-租户关联实体
    ├── dao/
    │   ├── TenantDao.kt                # 租户数据访问
    │   └── UserTenantDao.kt            # 用户-租户关联数据访问
    ├── service/
    │   └── TenantService.kt            # 租户业务逻辑
    ├── security/
    │   ├── TenantPrincipal.kt          # 包含租户信息的 Principal
    │   └── TenantAuthenticationToken.kt # 包含租户信息的 Token
    ├── filter/
    │   └── TenantAuthenticationFilter.kt # 租户信息注入过滤器
    ├── context/
    │   └── TenantContextHolder.kt      # 租户上下文持有者
    ├── utils/
    │   └── TenantUtils.kt              # 便捷工具类
    ├── configuration/
    │   ├── TenantSecurityConfiguration.kt  # Security 配置
    │   ├── TenantAutoConfiguration.kt       # 自动配置
    │   └── TenantFilterRegistration.kt      # 过滤器注册
    └── controller/
        └── TenantController.kt         # 租户信息查询接口
```

## 集成步骤

### 1. 数据库初始化

已更新 `database/init.sql`，添加了：
- `tenant` 表：存储租户信息
- `user_tenant` 表：用户与租户的关联关系
- 示例数据：4 个测试租户

```bash
cd database
docker-compose up -d
mysql -h localhost -u root -p spring-boot-kt < init.sql
```

### 2. 模块依赖

已在 `settings.gradle.kts` 中添加 tenant 模块：
```kotlin
include("tenant")
```

已在 `app/build.gradle.kts` 中添加依赖：
```kotlin
implementation(project(":tenant"))
```

### 3. Security 配置

已创建 `app/src/main/kotlin/com/vgerbot/app/configuration/AppSecurityConfiguration.kt`，
该配置：
- 替换了 `jwt-auth` 模块中的 `JwtConfiguration`
- 保留了 JWT 认证功能
- 添加了 `TenantAuthenticationFilter` 到过滤器链中

过滤器顺序：
```
JwtRequestFilter → TenantAuthenticationFilter → 业务处理
```

### 4. 排除原有配置

在 `Application.kt` 中排除 `JwtConfiguration`：
```kotlin
@SpringBootApplication(exclude = [JwtConfiguration::class])
```

## 使用方式

### 方式 1：使用 TenantUtils（最简单）

```kotlin
import com.vgerbot.tenant.utils.TenantUtils

val tenantId = TenantUtils.getCurrentTenantId()
val tenantCode = TenantUtils.getCurrentTenantCode()
```

### 方式 2：从 Principal 获取

```kotlin
import com.vgerbot.tenant.security.TenantPrincipal

@GetMapping("/info")
fun getInfo(@AuthenticationPrincipal principal: TenantPrincipal) {
    val tenantId = principal.tenantId
}
```

### 方式 3：从 SecurityContext 获取

```kotlin
import com.vgerbot.tenant.security.TenantAuthenticationToken

val auth = SecurityContextHolder.getContext().authentication
if (auth is TenantAuthenticationToken) {
    val tenantId = auth.tenantId
}
```

### 方式 4：从 ThreadLocal 获取

```kotlin
import com.vgerbot.tenant.context.TenantContextHolder

val tenantId = TenantContextHolder.getTenantId()
```

## 租户切换

用户可以通过请求头切换租户（需要用户属于目标租户）：

```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     http://localhost:8080/api/data
```

租户选择策略：
1. 如果请求头指定了 `X-Tenant-Id` 且用户属于该租户 → 使用指定租户
2. 否则使用用户的第一个租户
3. 如果用户没有租户 → `tenantId` 为 null

## API 测试

### 获取当前租户信息

```bash
GET /api/tenant/current
Authorization: Bearer <your-jwt-token>
```

响应示例：
```json
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
    "username": "testuser"
  },
  "fromUtils": {
    "tenantId": 1,
    "tenantCode": "tenant_demo",
    "tenantName": "演示租户"
  }
}
```

## 关键特性

✅ **无侵入设计**：完全不修改 `user`、`jwt-auth`、`rbac` 模块  
✅ **装饰器模式**：通过包装而非修改原有对象  
✅ **自动注入**：用户无感知，自动在 Principal 中添加租户信息  
✅ **灵活切换**：支持运行时通过请求头切换租户  
✅ **多种访问方式**：提供多种便捷方法获取租户信息  
✅ **线程安全**：使用 ThreadLocal 确保线程隔离  
✅ **自动清理**：请求结束自动清理 ThreadLocal，避免内存泄漏

## 架构优势

1. **解耦合**：tenant 模块独立，其他模块无需感知
2. **可扩展**：易于扩展租户相关功能（如租户级别配置、数据源切换等）
3. **可测试**：每个组件职责单一，便于单元测试
4. **向后兼容**：如果不使用 tenant 功能，不影响原有系统

## 后续优化建议

1. **添加缓存**：租户信息查询可以使用 Redis 缓存
2. **异步支持**：在异步场景下需要传递 ThreadLocal 上下文
3. **动态数据源**：实现基于租户的数据源路由
4. **租户级 RBAC**：将租户维度集成到权限系统中

详细使用方法请参阅 `tenant/README.md`。

