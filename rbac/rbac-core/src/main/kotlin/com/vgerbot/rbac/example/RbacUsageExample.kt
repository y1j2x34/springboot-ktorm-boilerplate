package com.vgerbot.rbac.example

import com.vgerbot.rbac.service.RbacService
import com.vgerbot.rbac.service.RoleService
import com.vgerbot.rbac.service.PermissionService
import com.vgerbot.rbac.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * RBAC 使用示例
 * 
 * 这个类展示了如何在代码中使用 RBAC 模块的各种功能
 */
@Component
class RbacUsageExample {
    
    @Autowired
    lateinit var rbacService: RbacService
    
    @Autowired
    lateinit var roleService: RoleService
    
    @Autowired
    lateinit var permissionService: PermissionService
    
    /**
     * 示例 1: 创建角色和权限
     */
    fun createRoleAndPermission() {
        // 创建一个编辑者角色
        val roleDto = CreateRoleDto(
            name = "编辑者",
            code = "ROLE_EDITOR",
            description = "内容编辑人员"
        )
        val role = roleService.createRole(roleDto)
        println("创建角色: ${role?.name}")
        
        // 创建一个编辑文章的权限
        val permissionDto = CreatePermissionDto(
            name = "编辑文章",
            code = "article:edit",
            resource = "article",
            action = "edit",
            description = "编辑文章内容"
        )
        val permission = permissionService.createPermission(permissionDto)
        println("创建权限: ${permission?.name}")
    }
    
    /**
     * 示例 2: 为角色分配权限
     */
    fun assignPermissionToRole(roleId: Int, permissionId: Int) {
        val assigned = rbacService.assignPermissionToRole(roleId, permissionId)
        if (assigned) {
            println("成功为角色分配权限")
        } else {
            println("权限已经分配给该角色")
        }
    }
    
    /**
     * 示例 3: 为用户分配角色
     */
    fun assignRoleToUser(userId: Int, roleId: Int) {
        val assigned = rbacService.assignRoleToUser(userId, roleId)
        if (assigned) {
            println("成功为用户分配角色")
        } else {
            println("角色已经分配给该用户")
        }
    }
    
    /**
     * 示例 4: 检查用户权限
     */
    fun checkUserPermission(userId: Int, permissionCode: String): Boolean {
        val hasPermission = rbacService.hasPermission(userId, permissionCode)
        if (hasPermission) {
            println("用户拥有权限: $permissionCode")
        } else {
            println("用户没有权限: $permissionCode")
        }
        return hasPermission
    }
    
    /**
     * 示例 5: 检查用户角色
     */
    fun checkUserRole(userId: Int, roleCode: String): Boolean {
        val hasRole = rbacService.hasRole(userId, roleCode)
        if (hasRole) {
            println("用户拥有角色: $roleCode")
        } else {
            println("用户没有角色: $roleCode")
        }
        return hasRole
    }
    
    /**
     * 示例 6: 获取用户的所有角色
     */
    fun getUserRoles(userId: Int) {
        val roles = rbacService.getUserRoles(userId)
        println("用户的角色列表:")
        roles.forEach { role ->
            println("  - ${role.name} (${role.code}): ${role.description}")
        }
    }
    
    /**
     * 示例 7: 获取用户的所有权限
     */
    fun getUserPermissions(userId: Int) {
        val permissions = rbacService.getUserPermissions(userId)
        println("用户的权限列表:")
        permissions.forEach { permission ->
            println("  - ${permission.name} (${permission.code}): ${permission.description}")
        }
    }
    
    /**
     * 示例 8: 获取角色的所有权限
     */
    fun getRolePermissions(roleId: Int) {
        val permissions = rbacService.getRolePermissions(roleId)
        println("角色的权限列表:")
        permissions.forEach { permission ->
            println("  - ${permission.name} (${permission.code})")
        }
    }
    
    /**
     * 示例 9: 完整的权限检查流程
     */
    fun completePermissionCheckFlow(userId: Int) {
        println("=== 用户权限检查 ===")
        println("用户ID: $userId")
        
        // 获取用户角色
        val roles = rbacService.getUserRoles(userId)
        println("\n用户角色:")
        roles.forEach { role ->
            println("  - ${role.name} (${role.code})")
        }
        
        // 获取用户权限
        val permissions = rbacService.getUserPermissions(userId)
        println("\n用户权限:")
        permissions.forEach { permission ->
            println("  - ${permission.code}: ${permission.name}")
        }
        
        // 检查特定权限
        println("\n权限检查结果:")
        val permissionsToCheck = listOf("user:read", "user:create", "user:delete")
        permissionsToCheck.forEach { code ->
            val hasPermission = rbacService.hasPermission(userId, code)
            println("  - $code: ${if (hasPermission) "✓ 允许" else "✗ 拒绝"}")
        }
    }
    
    /**
     * 示例 10: 批量权限管理
     */
    fun batchAssignPermissions(roleId: Int, permissionIds: List<Int>) {
        println("开始批量分配权限...")
        permissionIds.forEach { permissionId ->
            val assigned = rbacService.assignPermissionToRole(roleId, permissionId)
            if (assigned) {
                println("  ✓ 成功分配权限 ID: $permissionId")
            } else {
                println("  - 权限 ID: $permissionId 已存在，跳过")
            }
        }
        println("批量分配完成")
    }
}

