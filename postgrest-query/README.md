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
- `contains` - JSON 数组包含（MariaDB）

## Row Level Security (RLS)

模块集成了 authorization 模块，自动进行权限检查：

1. **权限检查**：在执行查询前，检查用户是否有权限访问表和执行操作
2. **RLS 条件**：根据用户权限自动添加 RLS 条件到 WHERE 子句
3. **多租户支持**：支持基于租户ID的 RLS

### RLS 策略示例

- **SELECT**：可以添加 `tenant_id = ?` 或 `created_by = ?` 条件
- **UPDATE/DELETE**：通常只允许用户操作自己创建的数据（`created_by = ?`）

## 安全注意事项

1. **SQL 注入防护**：所有表名和列名都经过清理，只允许字母、数字、下划线
2. **参数化查询**：所有用户输入都使用 PreparedStatement 参数化
3. **权限检查**：每次查询都会进行权限检查
4. **RLS 自动应用**：RLS 条件自动添加到查询中

## 配置

模块通过 Spring Boot 自动配置启用，无需额外配置。

如果需要自定义 RLS 行为，可以实现 `RowLevelSecurityProvider` 接口。
