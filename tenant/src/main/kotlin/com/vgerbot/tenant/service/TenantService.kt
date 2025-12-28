package com.vgerbot.tenant.service

import com.vgerbot.tenant.dao.TenantDao
import com.vgerbot.tenant.dao.UserTenantDao
import com.vgerbot.tenant.model.Tenant
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 租户服务
 * 
 * 提供租户信息查询和用户租户关联管理
 */
@Service
class TenantService {
    
    private val logger = LoggerFactory.getLogger(TenantService::class.java)
    
    @Autowired
    private lateinit var tenantDao: TenantDao
    
    @Autowired
    private lateinit var userTenantDao: UserTenantDao
    
    /**
     * 根据用户 ID 获取租户信息（返回第一个关联的租户）
     */
    fun getDefaultTenantForUser(userId: Int): Tenant? {
        val userTenant = userTenantDao.findDefaultByUserId(userId)
        if (userTenant == null) {
            logger.debug("User {} has no tenant", userId)
            return null
        }
        
        return tenantDao.findById(userTenant.tenantId)
    }
    
    /**
     * 根据用户 ID 获取所有租户信息
     */
    fun getTenantsForUser(userId: Int): List<Tenant> {
        val userTenants = userTenantDao.findByUserId(userId)
        return userTenants.mapNotNull { ut ->
            tenantDao.findById(ut.tenantId)
        }
    }
    
    /**
     * 根据租户 ID 获取租户信息
     */
    fun getTenantById(tenantId: Int): Tenant? {
        return tenantDao.findById(tenantId)
    }
    
    /**
     * 检查用户是否属于指定租户
     */
    fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean {
        return userTenantDao.findByUserIdAndTenantId(userId, tenantId) != null
    }
}

