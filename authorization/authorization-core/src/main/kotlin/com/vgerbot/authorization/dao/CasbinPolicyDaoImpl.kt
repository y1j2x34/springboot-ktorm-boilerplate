package com.vgerbot.authorization.dao

import com.vgerbot.authorization.entity.Permissions
import com.vgerbot.authorization.entity.RolePermissions
import com.vgerbot.authorization.entity.Roles
import com.vgerbot.authorization.entity.Tenants
import com.vgerbot.authorization.entity.UserPermissions
import com.vgerbot.authorization.entity.UserRoles
import com.vgerbot.authorization.entity.UserTenants
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class CasbinPolicyDaoImpl : CasbinPolicyDao {
    
    @Autowired
    private lateinit var database: Database
    
    override fun loadRolePermissionPolicies(): List<CasbinPolicyDao.RolePermissionPolicy> {
        return database
            .from(RolePermissions)
            .innerJoin(Roles, on = (RolePermissions.roleId eq Roles.id) and (Roles.status eq 1))
            .innerJoin(Permissions, on = (RolePermissions.permissionId eq Permissions.id) and (Permissions.status eq 1))
            .leftJoin(UserRoles, on = (UserRoles.roleId eq Roles.id) and (UserRoles.isDeleted eq false))
            .leftJoin(UserTenants, on = (UserTenants.userId eq UserRoles.userId) and (UserTenants.isDeleted eq false))
            .leftJoin(Tenants, on = (Tenants.id eq UserTenants.tenantId) and (Tenants.status eq 1))
            .select(
                Roles.code,
                Permissions.resource,
                Permissions.action,
                Tenants.id
            )
            .where {
                RolePermissions.isDeleted eq false
            }
            .groupBy(Roles.code, Permissions.resource, Permissions.action, Tenants.id)
            .map { row ->
                CasbinPolicyDao.RolePermissionPolicy(
                    roleCode = row[Roles.code]!!,
                    resource = row[Permissions.resource]!!,
                    action = row[Permissions.action]!!,
                    tenantId = row[Tenants.id]
                )
            }
    }
    
    override fun loadUserPermissionPolicies(): List<CasbinPolicyDao.UserPermissionPolicy> {
        return database
            .from(UserPermissions)
            .innerJoin(Permissions, on = (UserPermissions.permissionId eq Permissions.id) and (Permissions.status eq 1))
            .select(
                UserPermissions.userId,
                Permissions.resource,
                Permissions.action,
                UserPermissions.tenantId
            )
            .where {
                UserPermissions.isDeleted eq false
            }
            .map { row ->
                CasbinPolicyDao.UserPermissionPolicy(
                    userId = row[UserPermissions.userId]!!,
                    resource = row[Permissions.resource]!!,
                    action = row[Permissions.action]!!,
                    tenantId = row[UserPermissions.tenantId]
                )
            }
    }
    
    override fun loadUserRoleGroupings(): List<CasbinPolicyDao.UserRoleGrouping> {
        return database
            .from(UserRoles)
            .innerJoin(Roles, on = (UserRoles.roleId eq Roles.id) and (Roles.status eq 1))
            .leftJoin(UserTenants, on = (UserTenants.userId eq UserRoles.userId) and (UserTenants.isDeleted eq false))
            .leftJoin(Tenants, on = (Tenants.id eq UserTenants.tenantId) and (Tenants.status eq 1))
            .select(
                UserRoles.userId,
                Roles.code,
                Tenants.id
            )
            .where {
                UserRoles.isDeleted eq false
            }
            .map { row ->
                CasbinPolicyDao.UserRoleGrouping(
                    userId = row[UserRoles.userId]!!,
                    roleCode = row[Roles.code]!!,
                    tenantId = row[Tenants.id]
                )
            }
    }
    
    override fun loadUserTenantGroupings(): List<CasbinPolicyDao.UserTenantGrouping> {
        return database
            .from(UserTenants)
            .innerJoin(Tenants, on = (UserTenants.tenantId eq Tenants.id) and (Tenants.status eq 1))
            .select(
                UserTenants.userId,
                Tenants.id,
                Tenants.code
            )
            .where {
                (UserTenants.isDeleted eq false)
            }
            .map { row ->
                CasbinPolicyDao.UserTenantGrouping(
                    userId = row[UserTenants.userId]!!,
                    tenantId = row[Tenants.id]!!,
                    tenantCode = row[Tenants.code]!!
                )
            }
    }
}

