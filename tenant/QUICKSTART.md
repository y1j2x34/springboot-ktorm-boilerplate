# Tenant 模块 - 快速开始

## 5 分钟快速集成指南

### 1. 初始化数据库（1 分钟）

```bash
cd database
docker-compose up -d
mysql -h localhost -u root -proot spring-boot-kt < init.sql
```

数据库已包含：
- ✅ `tenant` 表（租户表）
- ✅ `user_tenant` 表（用户-租户关联）
- ✅ 4 个示例租户数据

### 2. 编译项目（2 分钟）

```bash
./gradlew build
```

### 3. 启动应用（1 分钟）

```bash
./gradlew :app:bootRun
```

### 4. 测试（1 分钟）

#### 步骤 1：登录获取 Token

```bash
curl -X POST http://localhost:8080/public/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

响应：
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 步骤 2：获取租户信息

```bash
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8080/api/tenant/current
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
  }
}
```

#### 步骤 3：测试示例接口

```bash
# 获取用户-租户信息
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8080/api/example/user-tenant-info

# 模拟数据隔离
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8080/api/example/products

# 检查租户状态
curl -H "Authorization: Bearer <your-token>" \
     http://localhost:8080/api/example/check-tenant
```

## 在你的代码中使用

### 方式 1：使用 TenantUtils（推荐）

```kotlin
import com.vgerbot.tenant.utils.TenantUtils

@RestController
class MyController {
    @GetMapping("/my-data")
    fun getMyData(): ResponseEntity<*> {
        val tenantId = TenantUtils.getCurrentTenantId()
        // 使用 tenantId 过滤数据
        return ResponseEntity.ok("Tenant: $tenantId")
    }
}
```

### 方式 2：使用 @AuthenticationPrincipal

```kotlin
import com.vgerbot.tenant.security.TenantPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal

@GetMapping("/info")
fun getInfo(@AuthenticationPrincipal principal: TenantPrincipal): Map<String, Any?> {
    return mapOf(
        "username" to principal.username,
        "tenantId" to principal.tenantId,
        "tenantCode" to principal.tenantCode
    )
}
```

### 方式 3：在 Service 层使用

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

## 高级功能

### 切换租户

通过请求头切换到其他租户（用户必须属于该租户）：

```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: 2" \
     http://localhost:8080/api/data
```

### 为用户分配租户

```sql
-- 为用户分配租户
INSERT INTO user_tenant (user_id, tenant_id) 
VALUES (1, 1);

-- 为用户分配多个租户
INSERT INTO user_tenant (user_id, tenant_id) 
VALUES (1, 2);
```

## 常见问题

### Q1: 租户信息为 null？

**原因**：用户没有分配租户

**解决**：
```sql
-- 检查用户是否有租户
SELECT * FROM user_tenant WHERE user_id = <your-user-id>;

-- 如果没有，分配一个租户
INSERT INTO user_tenant (user_id, tenant_id) 
VALUES (<your-user-id>, 1);
```

### Q2: 如何创建新租户？

```sql
-- 创建租户
INSERT INTO tenant (code, name, status) 
VALUES ('tenant_mycompany', '我的公司', 1);

-- 为用户分配新租户
INSERT INTO user_tenant (user_id, tenant_id) 
VALUES (<user-id>, LAST_INSERT_ID());
```

### Q3: 如何实现数据隔离？

在 DAO 层自动添加租户过滤：

```kotlin
@Repository
class ProductDao {
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

## 架构说明

```
请求 → JwtRequestFilter → TenantAuthenticationFilter → 业务逻辑
         (JWT 认证)         (注入租户信息)
```

- **JwtRequestFilter**：解析 JWT，创建基础的 Authentication
- **TenantAuthenticationFilter**：查询租户信息，增强 Authentication

## 详细文档

- 📖 [README.md](./README.md) - 完整使用文档
- 📖 [INTEGRATION.md](./INTEGRATION.md) - 集成指南
- 📖 [SUMMARY.md](./SUMMARY.md) - 实现总结

## 技术支持

如有问题，请查看：
1. 应用日志：`./logs`
2. TenantAuthenticationFilter 的日志输出
3. 数据库中的 `user_tenant` 表数据

祝使用愉快！🎉

