# Tenant 模块 - 多租户支持

> 在不修改 `user`、`jwt-auth`、`rbac` 模块的前提下，将租户信息无缝集成到 Spring Security 的 Principal 对象中。

## 📚 文档导航

### 快速开始
- **[QUICKSTART.md](./QUICKSTART.md)** - 5 分钟快速上手指南 ⚡
- **[INTEGRATION.md](./INTEGRATION.md)** - 集成指南和配置说明

### 详细文档
- **[README.md](./README.md)** - 完整使用手册 📖
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - 架构设计和原理说明
- **[SUMMARY.md](./SUMMARY.md)** - 实现总结和关键代码

## ✨ 核心特性

- ✅ **零侵入设计** - 完全不修改现有 user、jwt-auth、rbac 模块
- ✅ **自动注入** - JWT 认证后自动将租户信息注入到 Principal
- ✅ **装饰器模式** - 通过包装而非修改原有对象
- ✅ **灵活切换** - 支持通过请求头动态切换租户
- ✅ **多种访问方式** - 提供 4 种便捷方法获取租户信息
- ✅ **ThreadLocal 上下文** - 整个请求生命周期内可访问
- ✅ **线程安全** - 自动清理，避免内存泄漏

## 🚀 快速使用

### 在 Controller 中获取租户信息

```kotlin
import com.vgerbot.tenant.utils.TenantUtils
import com.vgerbot.tenant.security.TenantPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal

@RestController
class MyController {
    
    // 方式 1：使用工具类（最简单）
    @GetMapping("/data1")
    fun getData1(): String {
        val tenantId = TenantUtils.getCurrentTenantId()
        return "Tenant: $tenantId"
    }
    
    // 方式 2：使用 @AuthenticationPrincipal
    @GetMapping("/data2")
    fun getData2(@AuthenticationPrincipal principal: TenantPrincipal): Map<String, Any?> {
        return mapOf(
            "username" to principal.username,
            "tenantId" to principal.tenantId,
            "tenantCode" to principal.tenantCode
        )
    }
}
```

### 在 Service 中使用

```kotlin
import com.vgerbot.tenant.context.TenantContextHolder

@Service
class ProductService {
    fun getProducts(): List<Product> {
        val tenantId = TenantContextHolder.getTenantId()
        return productDao.findByTenantId(tenantId)
    }
}
```

### 切换租户

```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     http://localhost:8080/api/data
```

## 🏗️ 架构概览

```
HTTP Request (JWT Token)
    ↓
JwtRequestFilter (JWT 认证)
    ↓
TenantAuthenticationFilter (注入租户信息) ⭐
    ├─ 查询用户租户
    ├─ 包装为 TenantPrincipal
    ├─ 创建 TenantAuthenticationToken
    └─ 设置 ThreadLocal 上下文
    ↓
业务处理 (随时获取租户信息)
    ↓
Response (自动清理 ThreadLocal)
```

## 📦 模块结构

```
tenant/
├── model/              # 数据模型 (Tenant, UserTenant)
├── dao/                # 数据访问层
├── service/            # 业务逻辑 (TenantService)
├── security/           # 安全对象 (TenantPrincipal, TenantAuthenticationToken)
├── filter/             # 核心过滤器 (TenantAuthenticationFilter)
├── context/            # 上下文管理 (TenantContextHolder)
├── utils/              # 工具类 (TenantUtils)
├── configuration/      # 配置类
├── controller/         # API 接口
└── example/            # 使用示例
```

## 🎯 核心实现

### 1. 装饰器模式

```kotlin
// 包装原有的 UserDetails，添加租户信息
class TenantPrincipal(
    private val delegate: UserDetails,
    val tenantId: Int?,
    val tenantCode: String?,
    val tenantName: String?
) : UserDetails by delegate
```

### 2. 过滤器增强

```kotlin
// 在 JWT 认证后注入租户信息
@Component
class TenantAuthenticationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(...) {
        // 1. 获取已认证的用户
        // 2. 查询租户信息
        // 3. 包装 Principal
        // 4. 替换 Authentication
        // 5. 设置 ThreadLocal
    }
}
```

### 3. 集成配置

```kotlin
// app 模块中的 Security 配置
@Configuration
class AppSecurityConfiguration {
    @Bean
    fun configure(http: HttpSecurity) = http.run {
        addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        addFilterAfter(tenantAuthenticationFilter, JwtRequestFilter::class.java) // ⭐
    }.build()
}
```

## 🗄️ 数据库设计

### 租户表 (tenant)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键 |
| code | VARCHAR(50) | 租户代码（唯一） |
| name | VARCHAR(100) | 租户名称 |
| status | INT | 状态（1-启用，0-禁用） |

### 用户租户关联表 (user_tenant)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键 |
| user_id | INT | 用户 ID |
| tenant_id | INT | 租户 ID |

## 🔧 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/tenant/current` | GET | 获取当前租户信息 |
| `/api/example/user-tenant-info` | GET | 获取用户和租户信息 |
| `/api/example/products` | GET | 模拟租户数据隔离 |
| `/api/example/check-tenant` | GET | 检查租户状态 |

## 📋 文件清单

### 核心文件 ⭐

- `TenantAuthenticationFilter.kt` - 核心过滤器，实现租户信息注入
- `TenantPrincipal.kt` - 包含租户信息的 Principal
- `TenantAuthenticationToken.kt` - 包含租户信息的 Token
- `TenantService.kt` - 租户业务逻辑
- `TenantUtils.kt` - 便捷工具类

### 配置文件

- `app/.../AppSecurityConfiguration.kt` - 应用层 Security 配置
- `Application.kt` - 排除原有 JwtConfiguration

### 数据库

- `database/init.sql` - 包含租户表创建和示例数据

## 🎓 学习路径

1. **新手** → [QUICKSTART.md](./QUICKSTART.md) - 快速上手
2. **开发者** → [README.md](./README.md) - 详细使用指南
3. **架构师** → [ARCHITECTURE.md](./ARCHITECTURE.md) - 深入理解设计
4. **集成** → [INTEGRATION.md](./INTEGRATION.md) - 集成到现有项目
5. **总结** → [SUMMARY.md](./SUMMARY.md) - 实现回顾

## ❓ 常见问题

### Q: 租户信息为 null？
A: 检查用户是否在 `user_tenant` 表中有记录。

### Q: 如何添加新租户？
A: 在 `tenant` 表中插入记录，并在 `user_tenant` 中建立关联。

### Q: 如何切换租户？
A: 使用 `X-Tenant-Id` 请求头，前提是用户属于该租户。

### Q: 性能如何优化？
A: 添加 Redis 缓存租户信息，避免每次请求都查询数据库。

## 🔮 扩展建议

1. **添加缓存** - 使用 Spring Cache 或 Redis 缓存租户信息
2. **动态数据源** - 基于租户切换数据库
3. **租户级 RBAC** - 将租户维度集成到权限系统
4. **租户配置** - 为每个租户提供独立配置
5. **租户计费** - 记录租户的使用情况

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可

MIT License

---

**开始使用**: [QUICKSTART.md](./QUICKSTART.md) ⚡

