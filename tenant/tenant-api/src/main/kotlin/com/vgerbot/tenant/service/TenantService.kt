package com.vgerbot.tenant.com.vgerbot.tenant.service

import com.vgerbot.tenant.com.vgerbot.tenant.dto.TenantInfo

interface TenantService {
    fun getTenantById(id: Int): TenantInfo?
    fun getTenantByCode(code: String): TenantInfo?

    fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean

    fun assignUserToTenant(userId: Int, tenantCode: String)
    /**
     * 将用户分配到租户（通过租户ID）
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     */
    fun assignUserToTenant(userId: Int, tenantId: Int)
    /**
     * 根据邮箱查找匹配的租户
     *
     * @param email 用户邮箱
     * @return 匹配的租户
     */
    fun findTenantByEmail(email: String): TenantInfo?
    /**
     * 检查邮箱是否匹配租户配置的域名模式
     *
     * 支持的模式：
     * 1. 精确匹配：example.com
     * 2. 大括号扩展：comp.{com,cn} -> [comp.com, comp.cn]
     * 3. 多个域名用逗号分隔：example.com,test.com
     * 4. 通配符：*.example.com -> 匹配任意子域名
     *
     * @param email 邮箱地址
     * @param tenantId 租户ID
     * @return 是否匹配
     */
    fun isEmailMatchesTenant(email: String, tenantId: Int): Boolean
}