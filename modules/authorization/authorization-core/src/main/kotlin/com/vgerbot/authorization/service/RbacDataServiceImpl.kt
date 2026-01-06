package com.vgerbot.authorization.service

import com.vgerbot.authorization.dao.*
import com.vgerbot.authorization.dto.PermissionDto
import com.vgerbot.authorization.dto.RoleDto
import com.vgerbot.authorization.entity.*
import org.ktorm.dsl.inList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RbacDataServiceImpl : RbacDataService {
    
    @Autowired
    lateinit var userRoleDao: UserRoleDao
    
    @Autowired
    lateinit var roleDao: RoleDao
    
    @Autowired
    lateinit var permissionDao: PermissionDao
    
    @Autowired
    lateinit var rolePermissionDao: RolePermissionDao
    
    @Autowired
    lateinit var userPermissionDao: UserPermissionDao
    
    @Transactional
    override fun assignRoleToUser(userId: Int, roleId: Int): Boolean {
        // 检查是否已存在
        if (userRoleDao.existsByUserIdAndRoleId(userId, roleId)) {
            return false
        }
        
        val userRole = UserRole()
        userRole.userId = userId
        userRole.roleId = roleId
        userRole.createdAt = Instant.now()
        return userRoleDao.add(userRole) == 1
    }
    
    @Transactional
    override fun removeRoleFromUser(userId: Int, roleId: Int): Boolean {
        return userRoleDao.deleteByUserIdAndRoleId(userId, roleId) > 0
    }
    
    override fun getUserRoles(userId: Int): List<RoleDto> {
        val roleIds = userRoleDao.getRoleIdsByUserId(userId)
        
        if (roleIds.isEmpty()) return emptyList()
        
        return roleDao.findList { it.id inList roleIds }.map { it.toDto() }
    }
    
    override fun getUserPermissions(userId: Int): List<PermissionDto> {
        // 通过用户的角色查询所有权限
        val roleIds = userRoleDao.getRoleIdsByUserId(userId)
        
        if (roleIds.isEmpty()) return emptyList()
        
        val permissionIds = rolePermissionDao.getPermissionIdsByRoleIds(roleIds)
        
        if (permissionIds.isEmpty()) return emptyList()
        
        return permissionDao.findList { it.id inList permissionIds }.map { it.toDto() }
    }
    
    @Transactional
    override fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean {
        // 检查是否已存在
        if (rolePermissionDao.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return false
        }
        
        val rolePermission = RolePermission()
        rolePermission.roleId = roleId
        rolePermission.permissionId = permissionId
        rolePermission.createdAt = Instant.now()
        return rolePermissionDao.add(rolePermission) == 1
    }
    
    @Transactional
    override fun removePermissionFromRole(roleId: Int, permissionId: Int): Boolean {
        return rolePermissionDao.deleteByRoleIdAndPermissionId(roleId, permissionId) > 0
    }
    
    override fun getRolePermissions(roleId: Int): List<PermissionDto> {
        val permissionIds = rolePermissionDao.getPermissionIdsByRoleId(roleId)
        
        if (permissionIds.isEmpty()) return emptyList()
        
        return permissionDao.findList { it.id inList permissionIds }.map { it.toDto() }
    }
    
    // ==================== ACL: 用户直接权限管理 ====================
    
    @Transactional
    override fun assignPermissionToUser(userId: Int, permissionId: Int, tenantId: Int?): Boolean {
        // 检查是否已存在
        if (userPermissionDao.existsByUserIdAndPermissionId(userId, permissionId, tenantId)) {
            return false
        }
        
        val userPermission = UserPermission()
        userPermission.userId = userId
        userPermission.permissionId = permissionId
        userPermission.tenantId = tenantId
        userPermission.createdAt = Instant.now()
        return userPermissionDao.add(userPermission) == 1
    }
    
    @Transactional
    override fun removePermissionFromUser(userId: Int, permissionId: Int, tenantId: Int?): Boolean {
        return userPermissionDao.deleteByUserIdAndPermissionId(userId, permissionId, tenantId) > 0
    }
    
    override fun getUserDirectPermissions(userId: Int, tenantId: Int?): List<PermissionDto> {
        val permissionIds = if (tenantId != null) {
            userPermissionDao.getPermissionIdsByUserIdAndTenantId(userId, tenantId)
        } else {
            userPermissionDao.getPermissionIdsByUserId(userId)
        }
        
        if (permissionIds.isEmpty()) return emptyList()
        
        return permissionDao.findList { it.id inList permissionIds }.map { it.toDto() }
    }
    
    override fun getAllUserPermissions(userId: Int, tenantId: Int?): List<PermissionDto> {
        // 获取通过角色获得的权限
        val rolePermissions = getUserPermissions(userId)
        
        // 获取直接分配的权限
        val directPermissions = getUserDirectPermissions(userId, tenantId)
        
        // 合并去重（按权限ID去重）
        val allPermissions = (rolePermissions + directPermissions)
            .distinctBy { it.id }
        
        return allPermissions
    }
    
    // ==================== 权限检查 ====================
    
    override fun hasPermissionInDb(userId: Int, permissionCode: String): Boolean {
        return getAllUserPermissions(userId).any { it.code == permissionCode }
    }
    
    override fun hasRoleInDb(userId: Int, roleCode: String): Boolean {
        return getUserRoles(userId).any { it.code == roleCode }
    }
    
    override fun hasDirectPermissionInDb(userId: Int, permissionCode: String, tenantId: Int?): Boolean {
        return getUserDirectPermissions(userId, tenantId).any { it.code == permissionCode }
    }
}
