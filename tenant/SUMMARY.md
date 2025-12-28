# Tenant 模块实现总结

## 项目需求

在不修改 `user`、`jwt-auth`、`rbac` 模块的前提下，实现在 Spring Security 的 Principal 对象中插入当前登录用户的租户信息。

## 解决方案

### 核心思路

使用**装饰器模式**和**过滤器链扩展**，在 JWT 认证完成后，将租户信息注入到已有的认证对象中。

### 实现策略

1. **创建扩展对象**
   - `TenantPrincipal`：包装原有的 `UserDetails`，添加租户字段
   - `TenantAuthenticationToken`：扩展 `UsernamePasswordAuthenticationToken`，添加租户字段

2. **过滤器链增强**
   - `TenantAuthenticationFilter`：在 `JwtRequestFilter` 之后执行
   - 从 SecurityContext 获取已认证用户
   - 查询用户的租户信息
   - 将 Principal 包装为 `TenantPrincipal`
   - 创建 `TenantAuthenticationToken` 替换原有的 Authentication

3. **上下文管理**
   - `TenantContextHolder`：使用 ThreadLocal 存储租户信息
   - 在过滤器中设置，请求结束后自动清理

4. **应用层集成**
   - 在 `app` 模块创建 `AppSecurityConfiguration`
   - 排除 `jwt-auth` 模块的 `JwtConfiguration`
   - 在新配置中同时注册 JWT 和 Tenant 过滤器

## 实现细节

### 1. 数据模型

```
tenant (租户表)
├── id
├── code (租户代码)
├── name (租户名称)
├── status (状态)
├── created_at
└── updated_at

user_tenant (用户-租户关联表)
├── id
├── user_id
├── tenant_id
└── created_at
```

### 2. 代码结构

```
tenant/
├── model/              # 数据模型
├── dao/                # 数据访问层
├── service/            # 业务逻辑层
├── security/           # 安全对象（Principal、Token）
├── filter/             # 过滤器
├── context/            # 上下文管理
├── utils/              # 工具类
├── configuration/      # 配置类
├── controller/         # 示例控制器
└── example/            # 使用示例
```

### 3. 工作流程

```
HTTP Request (with JWT Token)
    ↓
JwtRequestFilter
    ├── 解析 JWT Token
    ├── 加载 UserDetails
    └── 创建 UsernamePasswordAuthenticationToken
    ↓
TenantAuthenticationFilter
    ├── 获取已认证的 Authentication
    ├── 提取用户名，查询用户 ID
    ├── 查询用户的租户信息
    │   ├── 优先使用请求头指定的租户（X-Tenant-Id）
    │   └── 否则使用默认租户
    ├── 包装 UserDetails 为 TenantPrincipal
    ├── 创建 TenantAuthenticationToken
    ├── 替换 SecurityContext 中的 Authentication
    └── 设置 ThreadLocal (TenantContextHolder)
    ↓
Business Logic
    ├── 从 @AuthenticationPrincipal 获取 TenantPrincipal
    ├── 从 TenantUtils 获取租户信息
    └── 从 TenantContextHolder 获取租户信息
    ↓
Response
    ↓
Filter Chain Complete
    └── TenantContextHolder.clear() (清理 ThreadLocal)
```

## 关键实现代码

### TenantPrincipal (装饰器模式)

```kotlin
class TenantPrincipal(
    private val delegate: UserDetails,
    val tenantId: Int?,
    val tenantCode: String?,
    val tenantName: String?
) : UserDetails {
    // 委托所有 UserDetails 方法到原对象
    override fun getUsername() = delegate.username
    override fun getPassword() = delegate.password
    // ... 其他方法
}
```

### TenantAuthenticationFilter (核心逻辑)

```kotlin
override fun doFilterInternal(...) {
    val authentication = SecurityContextHolder.getContext().authentication
    
    if (authentication != null && authentication.isAuthenticated) {
        val principal = authentication.principal as? UserDetails
        
        if (principal != null) {
            // 查询租户信息
            val tenant = getTenantForUser(userId, request)
            
            // 包装 Principal
            val tenantPrincipal = TenantPrincipal(principal, ...)
            
            // 创建新 Token
            val tenantAuth = TenantAuthenticationToken(tenantPrincipal, ...)
            
            // 替换 SecurityContext
            SecurityContextHolder.getContext().authentication = tenantAuth
            
            // 设置 ThreadLocal
            TenantContextHolder.setContext(...)
        }
    }
    
    try {
        filterChain.doFilter(request, response)
    } finally {
        TenantContextHolder.clear()
    }
}
```

### AppSecurityConfiguration (集成)

```kotlin
@Configuration
@EnableWebSecurity
class AppSecurityConfiguration {
    @Bean
    fun configure(http: HttpSecurity): DefaultSecurityFilterChain? =
        http.run {
            // ... 配置
            addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            addFilterAfter(tenantAuthenticationFilter, JwtRequestFilter::class.java)
        }.build()
}
```

## 使用方式

### 1. 在 Controller 中获取租户信息

```kotlin
// 方式 1
@GetMapping("/data")
fun getData(@AuthenticationPrincipal principal: TenantPrincipal) {
    val tenantId = principal.tenantId
}

// 方式 2
@GetMapping("/data")
fun getData() {
    val tenantId = TenantUtils.getCurrentTenantId()
}
```

### 2. 在 Service 中获取租户信息

```kotlin
@Service
class ProductService {
    fun getProducts(): List<Product> {
        val tenantId = TenantContextHolder.getTenantId()
        return productDao.findByTenantId(tenantId)
    }
}
```

### 3. 切换租户

```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     https://api.example.com/api/data
```

## 优势

1. ✅ **零侵入**：完全不修改现有模块
2. ✅ **松耦合**：租户模块独立，可随时移除
3. ✅ **可扩展**：易于添加新功能（如租户级配置、动态数据源等）
4. ✅ **类型安全**：使用强类型对象，编译时检查
5. ✅ **易用性**：提供多种便捷方法获取租户信息
6. ✅ **安全性**：租户切换时验证用户权限

## 测试验证

### 1. 启动应用

```bash
./gradlew :app:bootRun
```

### 2. 测试接口

```bash
# 获取当前租户信息
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/tenant/current

# 获取示例数据
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/example/products
```

## 文件清单

### 新创建的文件

```
tenant/
├── build.gradle.kts                                 # 模块配置
├── README.md                                         # 详细文档
├── INTEGRATION.md                                    # 集成指南
└── src/main/kotlin/com/vgerbot/tenant/
    ├── model/
    │   ├── Tenant.kt                                # 租户实体
    │   └── UserTenant.kt                            # 关联实体
    ├── dao/
    │   ├── TenantDao.kt                             # 租户 DAO
    │   └── UserTenantDao.kt                         # 关联 DAO
    ├── service/
    │   └── TenantService.kt                         # 租户服务
    ├── security/
    │   ├── TenantPrincipal.kt                       # 租户 Principal
    │   └── TenantAuthenticationToken.kt             # 租户 Token
    ├── filter/
    │   └── TenantAuthenticationFilter.kt            # 租户过滤器 ⭐
    ├── context/
    │   └── TenantContextHolder.kt                   # 上下文持有者
    ├── utils/
    │   └── TenantUtils.kt                           # 工具类
    ├── configuration/
    │   ├── TenantSecurityConfiguration.kt           # Security 配置
    │   ├── TenantAutoConfiguration.kt               # 自动配置
    │   └── TenantFilterRegistration.kt              # 过滤器注册
    ├── controller/
    │   └── TenantController.kt                      # 租户 API
    └── example/
        └── TenantExampleController.kt               # 使用示例 ⭐

app/src/main/kotlin/com/vgerbot/app/configuration/
└── AppSecurityConfiguration.kt                       # 应用 Security 配置 ⭐
```

### 修改的文件

```
settings.gradle.kts                     # 添加 tenant 模块
app/build.gradle.kts                    # 添加 tenant 依赖
app/src/main/kotlin/com/vgerbot/app/Application.kt  # 排除原 JwtConfiguration
database/init.sql                       # 添加租户表和数据
```

## 核心文件说明

- ⭐ `TenantAuthenticationFilter.kt` - **最核心**，实现租户信息注入
- ⭐ `AppSecurityConfiguration.kt` - **集成关键**，注册过滤器链
- ⭐ `TenantExampleController.kt` - **使用示例**，演示各种获取方式

## 总结

本实现通过装饰器模式和过滤器链扩展，成功地在不修改任何现有模块的情况下，将租户信息无缝集成到 Spring Security 的 Principal 对象中。整个设计遵循了 SOLID 原则，具有良好的可维护性和可扩展性。

