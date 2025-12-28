# Tenant 模块架构图

## 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        HTTP Request                              │
│                  (Authorization: Bearer <JWT>)                   │
│                  (X-Tenant-Id: <optional>)                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    JwtRequestFilter                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. 解析 Authorization 头                                   │  │
│  │ 2. 提取 JWT Token                                          │  │
│  │ 3. 验证 Token                                              │  │
│  │ 4. 加载 UserDetails (from CustomUserDetailsService)       │  │
│  │ 5. 创建 UsernamePasswordAuthenticationToken               │  │
│  │ 6. 设置到 SecurityContext                                  │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│               TenantAuthenticationFilter ⭐                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. 从 SecurityContext 获取已认证的 Authentication         │  │
│  │ 2. 提取用户名                                              │  │
│  │ 3. 通过 UserService 查询用户 ID                           │  │
│  │ 4. 查询租户信息 (TenantService)                           │  │
│  │    ├─ 优先：请求头中的 X-Tenant-Id                        │  │
│  │    │   └─ 验证用户是否属于该租户                          │  │
│  │    └─ 否则：用户的默认租户                                │  │
│  │ 5. 包装 UserDetails 为 TenantPrincipal                    │  │
│  │ 6. 创建 TenantAuthenticationToken                         │  │
│  │ 7. 替换 SecurityContext 中的 Authentication               │  │
│  │ 8. 设置 ThreadLocal (TenantContextHolder)                 │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Business Logic                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                 获取租户信息的多种方式                     │  │
│  │                                                             │  │
│  │  方式1: @AuthenticationPrincipal TenantPrincipal          │  │
│  │  方式2: TenantUtils.getCurrentTenantId()                  │  │
│  │  方式3: TenantContextHolder.getTenantId()                 │  │
│  │  方式4: SecurityContextHolder.getContext()...             │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Response                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Filter Chain Complete                           │
│              TenantContextHolder.clear()                         │
│                 (清理 ThreadLocal)                               │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件关系

```
┌──────────────────────────────────────────────────────────────┐
│                        tenant 模块                            │
│                                                                │
│  ┌──────────────┐       ┌──────────────────┐                │
│  │    Model     │       │   Security       │                 │
│  ├──────────────┤       ├──────────────────┤                 │
│  │ Tenant       │       │ TenantPrincipal  │ (装饰器)       │
│  │ UserTenant   │       │ TenantAuth...    │                 │
│  └──────────────┘       │   Token          │                 │
│         │               └──────────────────┘                 │
│         │                        │                            │
│         ▼                        │                            │
│  ┌──────────────┐               │                            │
│  │     DAO      │               │                            │
│  ├──────────────┤               │                            │
│  │ TenantDao    │               │                            │
│  │ UserTenant   │               │                            │
│  │   Dao        │               │                            │
│  └──────────────┘               │                            │
│         │                        │                            │
│         ▼                        │                            │
│  ┌──────────────┐               │                            │
│  │   Service    │               │                            │
│  ├──────────────┤               │                            │
│  │ TenantService│───────────────┘                            │
│  └──────────────┘               │                            │
│         │                        │                            │
│         │                        │                            │
│         │                        ▼                            │
│         │            ┌──────────────────┐                    │
│         │            │     Filter       │                    │
│         │            ├──────────────────┤                    │
│         └───────────▶│ TenantAuth...    │                    │
│                      │   Filter         │ ⭐核心            │
│                      └──────────────────┘                    │
│                               │                               │
│                               │                               │
│                               ▼                               │
│                      ┌──────────────────┐                    │
│                      │    Context       │                    │
│                      ├──────────────────┤                    │
│                      │ TenantContext    │ (ThreadLocal)      │
│                      │   Holder         │                    │
│                      └──────────────────┘                    │
│                               │                               │
│                               │                               │
│                               ▼                               │
│                      ┌──────────────────┐                    │
│                      │     Utils        │                    │
│                      ├──────────────────┤                    │
│                      │ TenantUtils      │ (便捷工具)        │
│                      └──────────────────┘                    │
└──────────────────────────────────────────────────────────────┘
```

## 数据流

### 1. 认证流程

```
User Login
    │
    ▼
JWT Auth ──────────────┐
    │                  │
    │                  ▼
    │          UsernamePasswordAuthenticationToken
    │          ┌────────────────────────┐
    │          │ principal: UserDetails │
    │          │ credentials: null      │
    │          │ authorities: []        │
    │          └────────────────────────┘
    │                  │
    ▼                  ▼
Tenant Filter
    │
    ▼
Query Tenant Info
    │
    ▼
TenantAuthenticationToken
┌─────────────────────────────────┐
│ principal: TenantPrincipal      │
│   ├─ delegate: UserDetails      │
│   ├─ tenantId: 1                │
│   ├─ tenantCode: "tenant_demo"  │
│   └─ tenantName: "演示租户"     │
│ credentials: null               │
│ authorities: []                 │
│ tenantId: 1                     │
│ tenantCode: "tenant_demo"       │
│ tenantName: "演示租户"          │
└─────────────────────────────────┘
    │
    ▼
Set to SecurityContext
Set to TenantContextHolder
```

### 2. 租户信息获取流程

```
Business Code
    │
    ├─────────────┬─────────────┬──────────────┐
    │             │             │              │
    ▼             ▼             ▼              ▼
@Authentication  TenantUtils   SecurityContext  ThreadLocal
Principal                      Holder           (TenantContext
                                                  Holder)
    │             │             │              │
    └─────────────┴─────────────┴──────────────┘
                   │
                   ▼
            Tenant Information
            ┌──────────────┐
            │ tenantId     │
            │ tenantCode   │
            │ tenantName   │
            └──────────────┘
```

## 装饰器模式实现

```
原始对象                     装饰后的对象
───────────                  ─────────────

UserDetails                  TenantPrincipal
┌──────────────┐            ┌────────────────────────┐
│ username     │            │ delegate: UserDetails  │
│ password     │    wrap    │ ├─ username            │
│ authorities  │   ────▶    │ ├─ password            │
│ enabled      │            │ ├─ authorities         │
│ ...          │            │ └─ ...                 │
└──────────────┘            │ tenantId               │
                            │ tenantCode             │
                            │ tenantName             │
                            └────────────────────────┘
                                     │
                                     │ 所有 UserDetails
                                     │ 方法委托给 delegate
                                     ▼
                            保持原有行为 + 新增租户信息
```

## 模块依赖关系

```
┌──────────┐
│   app    │
└────┬─────┘
     │
     ├──────────┬──────────┬──────────┬─────────┐
     │          │          │          │         │
     ▼          ▼          ▼          ▼         ▼
┌────────┐ ┌────────┐ ┌──────┐  ┌──────┐  ┌────────┐
│ tenant │ │  rbac  │ │ user │  │ jwt- │  │ common │
│        │ │        │ │      │  │ auth │  │        │
└───┬────┘ └────────┘ └──────┘  └──────┘  └────────┘
    │                      │         │          ▲
    └──────────────────────┴─────────┴──────────┘
                依赖 common 模块
```

## 扩展点

```
TenantAuthenticationFilter
         │
         ├─ 扩展点 1: 自定义租户选择策略
         │   └─ 可以从请求头、Cookie、Session 等获取
         │
         ├─ 扩展点 2: 添加租户验证逻辑
         │   └─ IP 白名单、时间限制等
         │
         ├─ 扩展点 3: 租户缓存
         │   └─ Redis、本地缓存等
         │
         └─ 扩展点 4: 动态数据源切换
             └─ 基于租户切换数据库
```

## 性能优化建议

```
原始实现 (每次请求)
┌──────────────────────────────────┐
│ Request                           │
│   ↓                               │
│ Query User (DB)                   │
│   ↓                               │
│ Query Tenant (DB)  ← 可优化      │
│   ↓                               │
│ Business Logic                    │
└──────────────────────────────────┘

优化后 (使用缓存)
┌──────────────────────────────────┐
│ Request                           │
│   ↓                               │
│ Query User (DB)                   │
│   ↓                               │
│ Query Tenant (Cache) ← 快速      │
│   ↓                               │
│ Business Logic                    │
└──────────────────────────────────┘

实现方式：
1. Spring Cache (@Cacheable)
2. Redis
3. Caffeine (本地缓存)
```

## 总结

1. **最小侵入**：只在 app 层创建新的 Security 配置
2. **装饰器模式**：包装而非修改原有对象
3. **过滤器链**：在 JWT 认证后自然地注入租户信息
4. **多种访问方式**：提供灵活的 API 满足不同场景
5. **线程安全**：ThreadLocal 确保线程隔离
6. **自动清理**：避免内存泄漏

这个设计既满足了需求，又保持了良好的架构和可维护性。

