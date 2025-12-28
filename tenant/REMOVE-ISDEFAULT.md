# 移除 isDefault 字段说明

## 变更内容

已从 tenant 模块中移除 `isDefault` 字段，简化租户关联逻辑。

## 数据库变更

### user_tenant 表结构

**之前：**
```sql
CREATE TABLE `user_tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `tenant_id` INT NOT NULL,
    `is_default` BOOLEAN NOT NULL DEFAULT FALSE,  -- 已移除
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tenant` (`user_id`, `tenant_id`)
);
```

**现在：**
```sql
CREATE TABLE `user_tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `tenant_id` INT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tenant` (`user_id`, `tenant_id`)
);
```

### 迁移 SQL（如果已有数据库）

```sql
-- 删除 is_default 字段
ALTER TABLE user_tenant DROP COLUMN is_default;

-- 删除相关索引（如果存在）
ALTER TABLE user_tenant DROP INDEX idx_is_default;
```

## 代码变更

### 1. UserTenant 实体

```kotlin
// 移除了 isDefault 字段
interface UserTenant : Entity<UserTenant> {
    val id: Int
    var userId: Int
    var tenantId: Int
    // var isDefault: Boolean  // 已移除
    var createdAt: Instant
}
```

### 2. UserTenantDao

```kotlin
/**
 * 根据用户 ID 查询租户（返回第一个关联的租户）
 */
fun findDefaultByUserId(userId: Int): UserTenant? {
    // 之前：查找 is_default = true 的记录
    // 现在：返回第一个找到的租户
    return database.userTenants.find { it.userId eq userId }
}
```

### 3. TenantService

```kotlin
/**
 * 根据用户 ID 获取租户信息（返回第一个关联的租户）
 */
fun getDefaultTenantForUser(userId: Int): Tenant? {
    // 逻辑保持不变，但注释更新
    val userTenant = userTenantDao.findDefaultByUserId(userId)
    if (userTenant == null) {
        logger.debug("User {} has no tenant", userId)
        return null
    }
    
    return tenantDao.findById(userTenant.tenantId)
}
```

## 行为变化

### 租户选择逻辑

**之前（有 isDefault）：**
1. 如果请求头指定 `X-Tenant-Id` → 使用指定租户
2. 否则查找 `is_default = true` 的租户
3. 如果没有默认租户 → 返回 null

**现在（无 isDefault）：**
1. 如果请求头指定 `X-Tenant-Id` → 使用指定租户
2. 否则返回用户的**第一个关联租户**（按数据库查询顺序）
3. 如果没有任何租户 → 返回 null

### 重要提示

⚠️ **对于有多个租户的用户：**
- 由于没有 `isDefault` 标记，系统会返回第一个查询到的租户
- 第一个租户的顺序**不确定**（取决于数据库查询顺序、ID 等）
- 建议通过请求头 `X-Tenant-Id` 明确指定租户

## 使用建议

### 单租户场景（推荐）

如果每个用户只属于一个租户，移除 `isDefault` 是合理的：

```kotlin
// 插入用户-租户关联
INSERT INTO user_tenant (user_id, tenant_id) 
VALUES (1, 1);

// 由于只有一个租户，不需要担心"默认"的概念
```

### 多租户场景（需要注意）

如果用户可能属于多个租户：

```kotlin
// 用户属于多个租户
INSERT INTO user_tenant (user_id, tenant_id) VALUES (1, 1);
INSERT INTO user_tenant (user_id, tenant_id) VALUES (1, 2);
INSERT INTO user_tenant (user_id, tenant_id) VALUES (1, 3);

// ⚠️ 不指定 X-Tenant-Id 时，会返回"第一个"租户（顺序不确定）
// ✅ 建议：总是通过请求头指定租户
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     http://localhost:8080/api/data
```

### 推荐方案：前端记住上次选择的租户

```typescript
// 前端实现
class TenantManager {
    // 登录后获取用户的所有租户
    async getUserTenants() {
        const tenants = await api.get('/api/tenant/my-tenants');
        
        // 从 localStorage 读取上次选择的租户
        const lastTenantId = localStorage.getItem('lastTenantId');
        
        // 如果上次的租户仍然有效，使用它
        if (lastTenantId && tenants.find(t => t.id === lastTenantId)) {
            return lastTenantId;
        }
        
        // 否则使用第一个租户
        return tenants[0]?.id;
    }
    
    // 发送请求时自动添加租户头
    async request(url, options = {}) {
        const tenantId = await this.getCurrentTenantId();
        return fetch(url, {
            ...options,
            headers: {
                ...options.headers,
                'X-Tenant-Id': tenantId
            }
        });
    }
}
```

## 数据迁移注意事项

如果你已经有生产数据并且使用了 `isDefault` 字段：

### 选项 1：保留现有的默认租户信息

```sql
-- 为每个用户保留最近使用的租户（基于 created_at）
-- 删除其他租户关联
DELETE FROM user_tenant
WHERE id NOT IN (
    SELECT MIN(id) FROM user_tenant GROUP BY user_id
);

-- 然后删除 is_default 字段
ALTER TABLE user_tenant DROP COLUMN is_default;
```

### 选项 2：按 is_default 保留

```sql
-- 保留标记为 is_default = true 的租户
-- 删除其他关联
DELETE FROM user_tenant WHERE is_default = false;

-- 然后删除 is_default 字段
ALTER TABLE user_tenant DROP COLUMN is_default;
```

### 选项 3：保留所有租户，让用户手动选择

```sql
-- 直接删除 is_default 字段
ALTER TABLE user_tenant DROP COLUMN is_default;

-- 前端提供租户选择器
```

## 总结

**移除 `isDefault` 的优点：**
- ✅ 简化数据模型
- ✅ 减少字段维护成本
- ✅ 适合单租户或前端管理租户选择的场景

**移除 `isDefault` 的缺点：**
- ❌ 多租户场景下用户体验略差（需要手动选择）
- ❌ 无法在后端明确"默认租户"的概念
- ❌ 依赖数据库查询顺序（不确定性）

**适用场景：**
- ✅ 每个用户只属于一个租户
- ✅ 前端应用会管理租户选择
- ✅ 所有请求都会带上 `X-Tenant-Id` 头

**不适用场景：**
- ❌ 需要明确的"主租户"概念
- ❌ 用户经常在多个租户间切换
- ❌ 需要自动选择最常用的租户

