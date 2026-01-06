# 通用审计实体重构示例

## 概述

本文档展示如何将现有代码重构为使用通用审计实体和逻辑删除功能。

## 示例 1：Role 实体的完整迁移

### 迁移前

**实体类（Role.kt）：**
```kotlin
package com.vgerbot.rbac.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

interface Role : Entity<Role> {
    companion object : Entity.Factory<Role>()
    
    val id: Int
    var name: String
    var code: String
    var description: String?
    var createdBy: Int?
    var createdAt: Instant
    var updatedBy: Int?
    var updatedAt: Instant?
    var isDeleted: Boolean
}

object Roles : Table<Role>("role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val description = varchar("description").bindTo { it.description }
    val createdBy = int("created_by").bindTo { it.createdBy }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedBy = int("updated_by").bindTo { it.updatedBy }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}
```

**DAO 类（RoleDaoImpl.kt）：**
```kotlin
package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : AbstractBaseDao<Role, Roles>(Roles), RoleDao
```

**服务类（RoleServiceImpl.kt）：**
```kotlin
@Transactional
override fun deleteRole(id: Int): Boolean {
    val role = roleDao.findOne { (it.id eq id) and (it.isDeleted eq false) } ?: return false
    role.isDeleted = true
    role.updatedAt = Instant.now()
    
    // 设置更新人
    val currentUsername = SecurityUtils.getCurrentUsername()
    if (currentUsername != null) {
        val currentUser = userService.findUser(currentUsername)
        currentUser?.let { role.updatedBy = it.id }
    }
    
    return roleDao.update(role) == 1
}

override fun getRoleById(id: Int): RoleDto? {
    return roleDao.findOne { (it.id eq id) and (it.isDeleted eq false) }?.toDto()
}

override fun getAllRoles(): List<RoleDto> {
    return roleDao.findList { it.isDeleted eq false }.map { it.toDto() }
}
```

### 迁移后

**实体类（Role.kt）：**
```kotlin
package com.vgerbot.rbac.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import org.ktorm.schema.*

// 实现 AuditableEntity，移除所有审计字段
interface Role : AuditableEntity<Role> {
    companion object : Entity.Factory<Role>()
    
    val id: Int
    var name: String
    var code: String
    var description: String?
    // 不再需要定义：createdBy, createdAt, updatedBy, updatedAt, isDeleted
}

// 继承 AuditableTable，移除所有审计字段绑定
object Roles : AuditableTable<Role>("role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val code = varchar("code").bindTo { it.code }
    val description = varchar("description").bindTo { it.description }
    // 不再需要绑定：createdBy, createdAt, updatedBy, updatedAt, isDeleted
}
```

**DAO 类（RoleDaoImpl.kt）：**
```kotlin
package com.vgerbot.rbac.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.rbac.entity.Role
import com.vgerbot.rbac.entity.Roles
import org.springframework.stereotype.Repository

@Repository
class RoleDaoImpl : AuditableDaoImpl<Role, Roles>(Roles), RoleDao {
    override fun getIdColumn() = tableObject.id
}
```

**服务类（RoleServiceImpl.kt）：**
```kotlin
// 删除方法：一行代码完成
@Transactional
override fun deleteRole(id: Int): Boolean {
    return roleDao.softDelete(id)  // 自动设置 isDeleted、updatedAt、updatedBy
}

// 查询方法：使用 findOneActive 自动过滤已删除记录
override fun getRoleById(id: Int): RoleDto? {
    return roleDao.findOneActive { it.id eq id }?.toDto()
}

// 列表查询：使用 findListActive 或 findAllActive
override fun getAllRoles(): List<RoleDto> {
    return roleDao.findAllActive().map { it.toDto() }
}
```

## 示例 2：UserRole 关联表的迁移

关联表通常不需要完整的审计功能（不需要 createdBy/updatedBy），使用 `SimpleAuditableEntity`：

### 迁移前

```kotlin
interface UserRole : Entity<UserRole> {
    companion object : Entity.Factory<UserRole>()
    
    val id: Int
    var userId: Int
    var roleId: Int
    var createdAt: Instant
    var isDeleted: Boolean
}

object UserRoles : Table<UserRole>("user_role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val roleId = int("role_id").bindTo { it.roleId }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}

@Repository
class UserRoleDaoImpl : AbstractBaseDao<UserRole, UserRoles>(UserRoles), UserRoleDao
```

### 迁移后

```kotlin
import com.vgerbot.common.entity.SimpleAuditableEntity
import com.vgerbot.common.entity.SimpleAuditableTable
import com.vgerbot.common.dao.SimpleAuditableDaoImpl

interface UserRole : SimpleAuditableEntity<UserRole> {
    companion object : Entity.Factory<UserRole>()
    
    val id: Int
    var userId: Int
    var roleId: Int
    // 不再需要定义：createdAt, isDeleted
}

object UserRoles : SimpleAuditableTable<UserRole>("user_role") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val roleId = int("role_id").bindTo { it.roleId }
    // 不再需要绑定：createdAt, isDeleted
}

@Repository
class UserRoleDaoImpl : SimpleAuditableDaoImpl<UserRole, UserRoles>(UserRoles), UserRoleDao {
    override fun getIdColumn() = tableObject.id
}
```

## 代码量对比

### 实体类
- **迁移前**：需要定义 5 个审计字段，每个字段 1 行，共 5 行
- **迁移后**：0 行（继承自 AuditableEntity）
- **节省**：每个实体节省 5 行代码

### 表定义
- **迁移前**：需要绑定 5 个审计字段，每个字段 1 行，共 5 行
- **迁移后**：0 行（继承自 AuditableTable）
- **节省**：每个表节省 5 行代码

### 服务层删除方法
- **迁移前**：10-15 行代码（查询、设置字段、获取用户、更新）
- **迁移后**：1 行代码（调用 softDelete）
- **节省**：每个删除方法节省 10-15 行代码

### 服务层查询方法
- **迁移前**：需要手动添加 `and (it.isDeleted eq false)` 条件
- **迁移后**：使用 `findOneActive`、`findAllActive` 自动过滤
- **节省**：每个查询方法节省 1-2 行代码

## 总结

对于一个包含 10 个实体的项目：
- **实体类**：节省约 50 行代码
- **表定义**：节省约 50 行代码
- **DAO 实现**：每个 DAO 增加 2 行（实现 getIdColumn），共 20 行
- **服务层**：节省约 100-150 行代码（假设每个实体有 1 个删除方法和 10 个查询方法）

**净节省：约 180-230 行代码**

更重要的是：
- **一致性**：所有审计字段命名统一
- **可维护性**：修改审计逻辑只需更新基类
- **可读性**：代码更简洁，意图更清晰
- **安全性**：自动设置审计信息，不会遗漏

