package com.vgerbot.tenant.service

import com.vgerbot.tenant.dto.TenantDto

interface TenantService {
    /**
     * 获取所有租户
     */
    fun getAllTenants(): List<TenantDto>
    
    /**
     * 根据 ID 获取租户
     */
    fun getTenantById(id: Int): TenantDto?
    
    /**
     * 根据 code 获取租户
     */
    fun getTenantByCode(code: String): TenantDto?

    /**
     * 检查用户是否属于某个租户
     */
    fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean

    /**
     * 将用户分配到租户（通过租户代码）
     */
    fun assignUserToTenant(userId: Int, tenantCode: String)
    
    /**
     * 将用户分配到租户（通过租户ID）
     */
    fun assignUserToTenant(userId: Int, tenantId: Int)
    
    /**
     * 获取用户的所有租户
     */
    fun getTenantsForUser(userId: Int): List<TenantDto>
    
    /**
     * 根据邮箱查找匹配的租户
     */
    fun findTenantByEmail(email: String): TenantDto?
    
    /**
     * 检查邮箱是否匹配租户配置的域名模式
     */
    fun isEmailMatchesTenant(email: String, tenantId: Int): Boolean
}
