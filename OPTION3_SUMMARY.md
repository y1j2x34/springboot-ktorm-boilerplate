# 选项3调整完成总结

## 调整策略

按照**选项3**的策略，我们区分了不同类型的实体：
- **主表**（Role、Permission、DictType、DictData）：使用 `status` 字段（启用/停用）
- **关联表**（UserRole、RolePermission、UserTenant）：使用逻辑删除（`isDeleted`）

## 已完成的工作

### 1. 创建 StatusAuditableEntity 基础类

#### StatusAuditableEntity 接口
**文件**: `/core/src/main/kotlin/com/vgerbot/common/entity/StatusAuditableEntity.kt`

```kotlin
interface StatusAuditableEntity<E : Entity<E>> : Entity<E> {
    var createdBy: Int?
    var createdAt: Instant
    var updatedBy: Int?
    var updatedAt: Instant?
    var status: Int  // 0-停用, 1-启用
}
```

#### StatusAuditableTable 抽象类
**文件**: `/core/src/main/kotlin/com/vgerbot/common/entity/StatusAuditableTable.kt`

自动绑定审计字段到数据库列，包括 `status` 字段。

#### StatusAuditableDaoImpl 实现类
**文件**: `/core/src/main/kotlin/com/vgerbot/common/dao/StatusAuditableDaoImpl.kt`

继承 `AbstractBaseDao`，提供基本的 CRUD 操作（不包含逻辑删除方法）。

### 2. 调整主表实体

以下实体已从 `AuditableEntity` 调整为 `StatusAuditableEntity`：

1. **Role** - 角色表
   - 移除 `isDeleted` 字段
   - 使用 `status` 字段（Int 类型：0-停用，1-启用）
   
2. **Permission** - 权限表
   - 移除 `isDeleted` 字段
   - 使用 `status` 字段
   
3. **DictType** - 字典类型表
   - 移除 `isDeleted` 字段
   - 使用 `status` 字段
   
4. **DictData** - 字典数据表
   - 移除 `isDeleted` 字段
   - 使用 `status` 字段

### 3. 保留关联表的逻辑删除

以下实体仍然使用 `SimpleAuditableEntity`，保留 `isDeleted` 字段：

1. **UserRole** - 用户角色关联表
   - 继续使用 `SimpleAuditableEntity`
   - 保留逻辑删除功能
   - 可以追踪历史分配记录
   
2. **RolePermission** - 角色权限关联表
   - 继续使用 `SimpleAuditableEntity`
   - 保留逻辑删除功能
   
3. **UserTenant** - 用户租户关联表
   - 继续使用 `SimpleAuditableEntity`
   - 保留逻辑删除功能

### 4. 调整 DAO 层

#### 主表 DAO（使用 StatusAuditableDaoImpl）
- `RoleDaoImpl`
- `PermissionDaoImpl`
- `DictTypeDaoImpl`
- `DictDataDaoImpl`

这些 DAO 不再继承 `SoftDeleteDao`，使用普通的 `BaseDao` 接口。

#### 关联表 DAO（使用 SimpleAuditableDaoImpl）
- `UserRoleDaoImpl`
- `RolePermissionDaoImpl`
- `UserTenantDaoImpl`

这些 DAO 继续使用 `SoftDeleteDao` 接口，提供逻辑删除功能。

### 5. 调整服务层

#### 主表服务层变化

**删除操作**：从逻辑删除改为停用

```kotlin
// 之前（逻辑删除）
override fun deleteRole(id: Int): Boolean {
    return roleDao.softDelete(id)
}

// 现在（停用）
override fun deleteRole(id: Int): Boolean {
    val role = roleDao.findOne { it.id eq id } ?: return false
    role.status = 0  // 停用
    role.updatedAt = Instant.now()
    return roleDao.update(role) == 1
}
```

**查询操作**：从过滤 `isDeleted` 改为过滤 `status`

```kotlin
// 之前
return roleDao.findOneActive { it.id eq id }?.toDto()

// 现在
return roleDao.findOne { (it.id eq id) and (it.status eq 1) }?.toDto()
```

**创建操作**：初始化 `status` 为 1

```kotlin
role.status = 1  // 默认启用
```

#### 关联表服务层

关联表的服务层继续使用 `softDelete` 和 `findOneActive` 等方法，保持不变。

## 优势分析

### 主表使用 status 的优势

1. **语义更清晰**
   - `status = 0/1` 比 `isDeleted = true/false` 更符合业务语义
   - "停用"比"删除"更准确地描述配置数据的状态

2. **更灵活的状态管理**
   - 可以扩展更多状态（如：草稿、审核中等）
   - `status` 是 Int 类型，便于扩展

3. **避免数据不一致**
   - 配置数据不应该"删除"，而应该"停用"
   - 已停用的配置仍然可以被引用，不会导致数据不一致

4. **更符合实际业务**
   - 角色、权限、字典通常只需要启用/停用
   - 真正需要删除时，可以直接物理删除

### 关联表使用逻辑删除的优势

1. **保留历史记录**
   - 可以追踪"谁在什么时候被分配了什么角色"
   - 对审计和合规性很重要

2. **支持撤销操作**
   - 可以恢复已删除的关联关系
   - 便于纠错

3. **数据分析**
   - 可以分析历史的权限分配情况
   - 有助于安全审计

## 数据库 status 字段说明

在数据库中，`status` 字段已经存在：
- **role** 表：有 `status` 字段（INT）
- **permission** 表：有 `status` 字段（INT）
- **dict_type** 表：有 `status` 字段（BOOLEAN，建议改为 INT）
- **dict_data** 表：有 `status` 字段（BOOLEAN，建议改为 INT）

建议将 DictType 和 DictData 的 `status` 字段从 BOOLEAN 改为 INT 类型，以保持一致性和扩展性。

## 使用示例

### 主表（以 Role 为例）

```kotlin
// 创建角色
val role = Role()
role.name = "管理员"
role.code = "ADMIN"
role.status = 1  // 默认启用
roleDao.add(role)

// 停用角色
val role = roleDao.findOne { it.id eq 1 }
role.status = 0
roleDao.update(role)

// 查询启用的角色
val activeRoles = roleDao.findList { it.status eq 1 }

// 如果真的需要删除，可以物理删除
roleDao.deleteIf { it.id eq 1 }
```

### 关联表（以 UserRole 为例）

```kotlin
// 分配角色
val userRole = UserRole()
userRole.userId = 1
userRole.roleId = 1
userRoleDao.add(userRole)

// 逻辑删除（移除角色）
userRoleDao.softDelete(userRole.id)

// 查询有效的用户角色关联
val activeUserRoles = userRoleDao.findAllActive()
```

## 总结

通过这次调整，我们实现了：

✅ **主表**（Role、Permission、DictType、DictData）使用 `status` 字段进行启用/停用管理  
✅ **关联表**（UserRole、RolePermission、UserTenant）使用逻辑删除保留历史记录  
✅ 代码语义更清晰，更符合业务实际  
✅ 保留了必要的历史追踪功能  
✅ 提高了代码的可维护性和扩展性

这种设计既避免了配置数据的误删除，又保留了关联关系的历史追踪能力，是一个平衡实用性和安全性的最佳方案。

