# PostgREST Query Module

支持 PostgREST 风格的动态查询模块，允许前端通过 JSON 配置执行数据库查询。

## 功能特性

- ✅ 支持 SELECT、INSERT、UPDATE、DELETE、UPSERT 操作
- ✅ 支持复杂的 WHERE 条件（AND、OR、嵌套条件）
- ✅ 支持排序（ORDER BY）
- ✅ 支持分页（LIMIT、RANGE）
- ✅ 支持计数查询（COUNT）
- ✅ 支持 Row Level Security (RLS)，基于 authorization 模块
- ✅ 自动权限检查
- ✅ **使用 Ktorm DSL 构建查询，确保多数据库兼容性**

## API 端点

### POST /api/postgrest/query

执行 PostgREST 风格的查询请求。

**请求体示例：**

```json
{
  "from": "users",
  "operation": "select",
  "select": ["id", "name", "email"],
  "where": [
    ["status", "eq", "active"],
    "and",
    ["age", "gte", 18]
  ],
  "order": {
    "created_at": { "ascending": false }
  },
  "limit": 10
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com"
      }
    ],
    "count": null,
    "head": false
  }
}
```

## 操作类型

### SELECT - 查询数据

```json
{
  "from": "users",
  "operation": "select",
  "select": ["*"],
  "where": [["status", "eq", "active"]],
  "order": {
    "created_at": { "ascending": false }
  },
  "limit": 10,
  "count": "exact"
}
```

### INSERT - 插入数据

```json
{
  "from": "users",
  "operation": "insert",
  "data": {
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30
  },
  "select": ["*"]
}
```

### UPDATE - 更新数据

```json
{
  "from": "users",
  "operation": "update",
  "data": {
    "status": "inactive"
  },
  "where": [["id", "eq", 1]],
  "select": ["*"]
}
```

### DELETE - 删除数据

```json
{
  "from": "users",
  "operation": "delete",
  "where": [["id", "eq", 1]]
}
```

### UPSERT - 插入或更新

```json
{
  "from": "users",
  "operation": "upsert",
  "data": {
    "email": "john@example.com",
    "name": "John Updated"
  },
  "onConflict": "email",
  "select": ["*"]
}
```

## WHERE 条件操作符

支持以下操作符：

- `eq` - 等于
- `neq` - 不等于
- `gt` - 大于
- `gte` - 大于等于
- `lt` - 小于
- `lte` - 小于等于
- `like` - LIKE 匹配
- `ilike` - 不区分大小写 LIKE 匹配
- `is` - IS NULL / IS NOT NULL
- `in` - IN 列表

## Row Level Security (RLS)

模块集成了 authorization 模块，自动进行权限检查：

1. **权限检查**：在执行查询前，检查用户是否有权限访问表和执行操作
2. **RLS 条件**：根据用户权限自动添加 RLS 条件到 WHERE 子句
3. **多租户支持**：支持基于租户ID的 RLS

### RLS 策略示例

- **SELECT**：可以添加 `tenant_id = ?` 或 `created_by = ?` 条件
- **UPDATE/DELETE**：通常只允许用户操作自己创建的数据（`created_by = ?`）

## 安全注意事项

1. **表注册控制**：只有注册过的表才能被查询，防止未授权访问
2. **参数化查询**：使用 Ktorm DSL 构建查询，自动参数化
3. **权限检查**：每次查询都会进行权限检查
4. **RLS 自动应用**：RLS 条件自动添加到查询中

## 配置

### 1. 注册可查询的表

在使用查询 API 之前，需要先注册可查询的表。可以通过实现 `ApplicationRunner` 或在配置类中注册：

```kotlin
@Configuration
class PostgrestTableConfiguration(
    private val tableRegistry: TableRegistry
) {
    
    @PostConstruct
    fun registerTables() {
        // 注册 User 表
        tableRegistry.registerTable("users", Users)
        
        // 注册其他表
        tableRegistry.registerTable("orders", Orders)
        tableRegistry.registerTable("products", Products)
    }
}
```

### 2. 自动配置

模块通过 Spring Boot 自动配置启用，会自动扫描并注册以下组件：
- `DefaultTableRegistry` - 表注册表
- `KtormQueryBuilder` - 查询构建器
- `RowLevelSecurityProvider` - RLS 提供者
- `PostgrestQueryServiceImpl` - 查询服务
- `PostgrestQueryController` - REST 控制器

### 3. 自定义 RLS

如果需要自定义 RLS 行为，可以创建自己的 `RowLevelSecurityProvider` Bean：

```kotlin
@Component
@Primary
class CustomRlsProvider(
    private val authorizationService: AuthorizationService
) : RowLevelSecurityProvider(authorizationService) {
    
    override fun getRlsConditions(
        tableName: String,
        userId: String,
        tenantId: String?,
        operation: String
    ): List<RlsCondition> {
        // 自定义 RLS 逻辑
        return super.getRlsConditions(tableName, userId, tenantId, operation)
    }
}
```

## 数据库兼容性

本模块使用 Ktorm DSL 构建查询，自动处理不同数据库的 SQL 方言差异：

- ✅ MySQL / MariaDB
- ✅ PostgreSQL
- ✅ SQLite
- ✅ Oracle
- ✅ SQL Server

只需配置对应的 Ktorm 数据库方言即可。
