# 通用审计实体和逻辑删除使用指南

## 概述

为了减少重复代码，系统提供了通用的审计实体接口和抽象 DAO 实现，支持：
- 统一的审计字段（创建人、创建时间、更新人、更新时间）
- 逻辑删除功能
- 自动设置审计信息

## 核心组件

### 1. 实体接口

#### AuditableEntity
完整的审计实体接口，包含所有审计字段：
```kotlin
interface Role : AuditableEntity<Role> {
    companion object : Entity.Factory<Role>()
    
    val id: Int
    var name: String
    var code: String
    var description: String?
    
    // 以下字段由 AuditableEntity 提供，无需重复定义：
    // var createdBy: Int?
    // var createdAt: Instant
    // var updatedBy: Int?
    // var updatedAt: Instant?
    // var isDeleted: Boolean
}
```

#### SimpleAuditableEntity
简单的审计实体接口（适用于关联表等不需要审计人员的场景）：
```kotlin
interface UserRole : SimpleAuditableEntity<UserRole> {
    companion object : Entity.Factory<UserRole>()
    
    val id: Int
    var userId: Int
    var roleId: Int
    
    // 以下字段由 SimpleAuditableEntity 提供：
    // var createdAt: Instant
    // var isDeleted: Boolean
}
```

### 2. 表定义

#### AuditableTable
完整的审计表抽象类：
```kotlin
object Roles : AuditableTable<Role>("role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val description = varchar("description").bindTo { it.description }
    
    // 以下字段由 AuditableTable 提供，无需重复定义：
    // val createdBy = int("created_by").bindTo { it.createdBy }
    // val createdAt = timestamp("created_at").bindTo { it.createdAt }
    // val updatedBy = int("updated_by").bindTo { it.updatedBy }
    // val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    // val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}
```

#### SimpleAuditableTable
简单的审计表抽象类：
```kotlin
object UserRoles : SimpleAuditableTable<UserRole>("user_role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val roleId = int("role_id").bindTo { it.roleId }
    
    // 以下字段由 SimpleAuditableTable 提供：
    // val createdAt = timestamp("created_at").bindTo { it.createdAt }
    // val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}
```

### 3. DAO 实现

#### AuditableDaoImpl
完整的审计 DAO 实现：
```kotlin
@Repository
class RoleDaoImpl : AuditableDaoImpl<Role, Roles>(Roles), RoleDao {
    override fun getIdColumn() = tableObject.id
}
```

#### SimpleAuditableDaoImpl
简单的审计 DAO 实现：
```kotlin
@Repository
class UserRoleDaoImpl : SimpleAuditableDaoImpl<UserRole, UserRoles>(UserRoles), UserRoleDao {
    override fun getIdColumn() = tableObject.id
}
```

## DAO 方法

### 逻辑删除方法

```kotlin
// 按 ID 逻辑删除（自动设置 isDeleted=true, updatedAt, updatedBy）
roleDao.softDelete(1)

// 按条件逻辑删除
roleDao.softDeleteIf { it.code eq "OLD_ROLE" }
```

### 查询未删除记录

```kotlin
// 查找单个未删除记录
val role = roleDao.findOneActive { it.id eq 1 }

// 查找多个未删除记录
val roles = roleDao.findListActive { it.name like "%admin%" }

// 查找所有未删除记录
val allRoles = roleDao.findAllActive()

// 统计未删除记录数
val count = roleDao.countActive { it.status eq true }
```

### 传统方法（仍然可用）

```kotlin
// 这些方法仍然可用，但不会自动过滤已删除记录
roleDao.findOne { it.id eq 1 }
roleDao.findList { it.name like "%admin%" }
roleDao.findAll()
```

## 服务层使用示例

### 创建实体

```kotlin
@Transactional
override fun createRole(dto: CreateRoleDto): RoleDto? {
    val role = Role()
    role.name = dto.name
    role.code = dto.code
    role.description = dto.description
    role.createdAt = Instant.now()
    role.isDeleted = false
    
    // 设置创建人（如果有认证上下文）
    val currentUsername = SecurityUtils.getCurrentUsername()
    if (currentUsername != null) {
        val currentUser = userService.findUser(currentUsername)
        currentUser?.let { role.createdBy = it.id }
    }
    
    return if (roleDao.add(role) == 1) role.toDto() else null
}
```

### 更新实体

```kotlin
@Transactional
override fun updateRole(id: Int, dto: UpdateRoleDto): Boolean {
    // 使用 findOneActive 自动过滤已删除记录
    val role = roleDao.findOneActive { it.id eq id } ?: return false
    
    dto.name?.let { role.name = it }
    dto.code?.let { role.code = it }
    role.updatedAt = Instant.now()
    
    // 设置更新人
    val currentUsername = SecurityUtils.getCurrentUsername()
    if (currentUsername != null) {
        val currentUser = userService.findUser(currentUsername)
        currentUser?.let { role.updatedBy = it.id }
    }
    
    return roleDao.update(role) == 1
}
```

### 删除实体

```kotlin
@Transactional
override fun deleteRole(id: Int): Boolean {
    // 使用 softDelete 自动设置 isDeleted、updatedAt、updatedBy
    return roleDao.softDelete(id)
}
```

### 查询实体

```kotlin
override fun getRoleById(id: Int): RoleDto? {
    // 使用 findOneActive 自动过滤已删除记录
    return roleDao.findOneActive { it.id eq id }?.toDto()
}

override fun getAllRoles(): List<RoleDto> {
    // 使用 findAllActive 自动过滤已删除记录
    return roleDao.findAllActive().map { it.toDto() }
}
```

## 优势

1. **减少重复代码**：审计字段定义一次，所有实体共享
2. **统一规范**：所有审计字段命名和类型统一
3. **自动化**：逻辑删除自动设置审计信息
4. **类型安全**：编译时检查，避免拼写错误
5. **易于维护**：修改审计逻辑只需更新基类
6. **向后兼容**：不使用新方法的代码仍可正常工作

## 迁移指南

### 步骤 1：更新实体接口

将：
```kotlin
interface Role : Entity<Role> {
    var createdBy: Int?
    var createdAt: Instant
    var updatedBy: Int?
    var updatedAt: Instant?
    var isDeleted: Boolean
}
```

改为：
```kotlin
interface Role : AuditableEntity<Role> {
    // 移除审计字段定义
}
```

### 步骤 2：更新表定义

将：
```kotlin
object Roles : Table<Role>("role") {
    val createdBy = int("created_by").bindTo { it.createdBy }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedBy = int("updated_by").bindTo { it.updatedBy }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}
```

改为：
```kotlin
object Roles : AuditableTable<Role>("role") {
    // 移除审计字段绑定
}
```

### 步骤 3：更新 DAO 实现

将：
```kotlin
@Repository
class RoleDaoImpl : AbstractBaseDao<Role, Roles>(Roles), RoleDao
```

改为：
```kotlin
@Repository
class RoleDaoImpl : AuditableDaoImpl<Role, Roles>(Roles), RoleDao {
    override fun getIdColumn() = tableObject.id
}
```

### 步骤 4：简化服务层

将：
```kotlin
@Transactional
override fun deleteRole(id: Int): Boolean {
    val role = roleDao.findOne { (it.id eq id) and (it.isDeleted eq false) } ?: return false
    role.isDeleted = true
    role.updatedAt = Instant.now()
    // 设置更新人...
    return roleDao.update(role) == 1
}
```

改为：
```kotlin
@Transactional
override fun deleteRole(id: Int): Boolean {
    return roleDao.softDelete(id)
}
```

## 注意事项

1. 如果实体不需要完整的审计功能，使用 `SimpleAuditableEntity` 和 `SimpleAuditableTable`
2. `softDelete` 方法会自动设置 `updatedAt` 和 `updatedBy`（如果有认证上下文）
3. 使用 `findOneActive`、`findListActive` 等方法自动过滤已删除记录
4. 原有的 `findOne`、`findList` 等方法仍然可用，但不会过滤已删除记录

