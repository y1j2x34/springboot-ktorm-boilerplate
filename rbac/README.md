# RBAC 模块

基于角色的访问控制 (Role-Based Access Control) 模块，用于管理用户角色和权限。

## 功能特性

- ✅ 角色管理（创建、更新、删除、查询）
- ✅ 权限管理（创建、更新、删除、查询）
- ✅ 用户角色关联（分配、移除、查询）
- ✅ 角色权限关联（分配、移除、查询）
- ✅ 权限检查（用户是否拥有某个权限或角色）
- ✅ 完全独立于 user 模块，通过 userId 进行关联

## 架构设计

### 数据模型

```
User (user模块)
  ↓ (userId)
UserRole (关联表)
  ↓ (roleId)
Role
  ↓ (roleId)
RolePermission (关联表)
  ↓ (permissionId)
Permission
```

### 核心概念

- **Role (角色)**: 用户的身份类型，如管理员、普通用户等
- **Permission (权限)**: 对资源的操作权限，如 `user:read`, `user:create` 等
- **UserRole (用户角色)**: 用户和角色的多对多关联
- **RolePermission (角色权限)**: 角色和权限的多对多关联

## API 端点

### 角色管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/roles` | 创建角色 |
| PUT | `/api/roles/{id}` | 更新角色 |
| DELETE | `/api/roles/{id}` | 删除角色 |
| GET | `/api/roles/{id}` | 根据 ID 查询角色 |
| GET | `/api/roles/code/{code}` | 根据代码查询角色 |
| GET | `/api/roles` | 获取所有角色 |

### 权限管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/permissions` | 创建权限 |
| PUT | `/api/permissions/{id}` | 更新权限 |
| DELETE | `/api/permissions/{id}` | 删除权限 |
| GET | `/api/permissions/{id}` | 根据 ID 查询权限 |
| GET | `/api/permissions/code/{code}` | 根据代码查询权限 |
| GET | `/api/permissions` | 获取所有权限 |

### RBAC 管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/rbac/users/roles` | 为用户分配角色 |
| DELETE | `/api/rbac/users/roles` | 移除用户的角色 |
| GET | `/api/rbac/users/{userId}/roles` | 获取用户的所有角色 |
| GET | `/api/rbac/users/{userId}/permissions` | 获取用户的所有权限 |
| POST | `/api/rbac/roles/permissions` | 为角色分配权限 |
| DELETE | `/api/rbac/roles/permissions` | 移除角色的权限 |
| GET | `/api/rbac/roles/{roleId}/permissions` | 获取角色的所有权限 |
| GET | `/api/rbac/users/{userId}/has-permission/{code}` | 检查用户是否有某个权限 |
| GET | `/api/rbac/users/{userId}/has-role/{code}` | 检查用户是否有某个角色 |

## 使用示例

### 1. 创建角色

```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "编辑者",
    "code": "ROLE_EDITOR",
    "description": "内容编辑人员"
  }'
```

### 2. 创建权限

```bash
curl -X POST http://localhost:8080/api/permissions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "编辑文章",
    "code": "article:edit",
    "resource": "article",
    "action": "edit",
    "description": "编辑文章内容"
  }'
```

### 3. 为用户分配角色

```bash
curl -X POST http://localhost:8080/api/rbac/users/roles \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roleId": 2
  }'
```

### 4. 为角色分配权限

```bash
curl -X POST http://localhost:8080/api/rbac/roles/permissions \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": 2,
    "permissionId": 3
  }'
```

### 5. 检查用户权限

```bash
# 检查用户是否有某个权限
curl http://localhost:8080/api/rbac/users/1/has-permission/article:edit

# 检查用户是否有某个角色
curl http://localhost:8080/api/rbac/users/1/has-role/ROLE_EDITOR
```

### 6. 获取用户的所有权限

```bash
curl http://localhost:8080/api/rbac/users/1/permissions
```

## 在代码中使用

### 注入 RbacService

```kotlin
@RestController
class MyController {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    @GetMapping("/protected-resource")
    fun protectedResource(@RequestParam userId: Int): ResponseEntity<String> {
        // 检查用户是否有权限
        if (!rbacService.hasPermission(userId, "article:edit")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("您没有权限访问此资源")
        }
        
        // 执行业务逻辑
        return ResponseEntity.ok("访问成功")
    }
}
```

### 获取用户角色和权限

```kotlin
// 获取用户的所有角色
val roles = rbacService.getUserRoles(userId)
roles.forEach { role ->
    println("角色: ${role.name} (${role.code})")
}

// 获取用户的所有权限
val permissions = rbacService.getUserPermissions(userId)
permissions.forEach { permission ->
    println("权限: ${permission.name} (${permission.code})")
}
```

## 数据库初始化

执行 `/database/init.sql` 脚本来创建表结构和初始数据：

```sql
-- 创建 role, permission, user_role, role_permission 表
-- 插入默认角色：ROLE_ADMIN, ROLE_USER
-- 插入默认权限：user:*, role:*, permission:*
-- 为管理员分配所有权限
```

默认初始化数据：

- **角色**:
  - `ROLE_ADMIN` - 管理员（拥有所有权限）
  - `ROLE_USER` - 普通用户（只有基础查看权限）

- **权限**:
  - 用户管理: `user:read`, `user:create`, `user:update`, `user:delete`
  - 角色管理: `role:read`, `role:create`, `role:update`, `role:delete`
  - 权限管理: `permission:read`, `permission:create`, `permission:update`, `permission:delete`

## 与 User 模块的关系

**重要**: 此 RBAC 模块完全独立于 user 模块，不需要修改 user 模块的任何代码。

- 通过 `userId` (整数) 关联用户
- 不直接依赖 User 实体
- 可以与任何用户系统集成

## 权限命名规范

建议使用以下格式命名权限：

```
resource:action
```

例如：
- `user:read` - 读取用户
- `user:create` - 创建用户
- `article:edit` - 编辑文章
- `order:delete` - 删除订单

## 与 Spring Security 集成

可以创建一个自定义的 `UserDetailsService` 来集成 RBAC 权限：

```kotlin
@Service
class RbacUserDetailsService : UserDetailsService {
    
    @Autowired
    lateinit var userService: UserService
    
    @Autowired
    lateinit var rbacService: RbacService
    
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.findUser(username) 
            ?: throw UsernameNotFoundException("User not found")
        
        // 获取用户的所有权限
        val permissions = rbacService.getUserPermissions(user.id)
        val authorities = permissions.map { 
            SimpleGrantedAuthority(it.code) 
        }
        
        return User(user.username, user.password, authorities)
    }
}
```

## 注意事项

1. **外键约束**: `user_role` 表通过外键引用 `user` 表，确保数据一致性
2. **级联删除**: 删除用户时会自动删除相关的角色关联
3. **唯一约束**: 用户-角色、角色-权限关联都有唯一约束，防止重复分配
4. **权限代码**: 建议使用统一的命名规范，便于管理和检查

## 扩展建议

1. **数据权限**: 可以扩展 Permission 添加 `scope` 字段，支持数据级权限控制
2. **权限继承**: 实现角色继承机制
3. **动态权限**: 支持在运行时动态添加权限
4. **权限缓存**: 使用 Redis 缓存用户权限信息，提高性能
5. **审计日志**: 记录角色和权限的变更历史

