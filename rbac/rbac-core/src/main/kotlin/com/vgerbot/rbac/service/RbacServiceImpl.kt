package com.vgerbot.rbac.service

import com.vgerbot.rbac.dao.*
import com.vgerbot.rbac.dto.PermissionDto
import com.vgerbot.rbac.dto.RoleDto
import com.vgerbot.rbac.entity.*
import org.ktorm.dsl.inList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RbacServiceImpl : RbacService {
    
    @Autowired
    lateinit var userRoleDao: UserRoleDao
    
    @Autowired
    lateinit var roleDao: RoleDao
    
    @Autowired
    lateinit var permissionDao: PermissionDao
    
    @Autowired
    lateinit var rolePermissionDao: RolePermissionDao
    
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
    
    override fun hasPermission(userId: Int, permissionCode: String): Boolean {
        return getUserPermissions(userId).any { it.code == permissionCode }
    }
    
    override fun hasRole(userId: Int, roleCode: String): Boolean {
        return getUserRoles(userId).any { it.code == roleCode }
    }
}

