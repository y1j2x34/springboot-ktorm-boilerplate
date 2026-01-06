package com.vgerbot.tenant.service

import com.vgerbot.common.utils.EmailDomainMatcher
import com.vgerbot.tenant.dao.TenantDao
import com.vgerbot.tenant.dao.UserTenantDao
import com.vgerbot.tenant.dto.TenantDto
import com.vgerbot.tenant.entity.UserTenant
import com.vgerbot.tenant.entity.toDto
import org.ktorm.dsl.eq
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 租户服务实现
 */
@Service
class TenantServiceImpl : TenantService {

    private val logger = LoggerFactory.getLogger(TenantServiceImpl::class.java)

    @Autowired
    private lateinit var tenantDao: TenantDao

    @Autowired
    private lateinit var userTenantDao: UserTenantDao

    override fun getAllTenants(): List<TenantDto> {
        return tenantDao.findList { it.status eq 1 }.map { it.toDto() }
    }

    override fun getTenantById(id: Int): TenantDto? {
        return tenantDao.findById(id)?.toDto()
    }

    override fun getTenantByCode(code: String): TenantDto? {
        return tenantDao.findOne { it.code eq code }?.toDto()
    }

    override fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean {
        return userTenantDao.findByUserIdAndTenantId(userId, tenantId) != null
    }

    @Transactional
    override fun assignUserToTenant(userId: Int, tenantCode: String) {
        val tenant = tenantDao.findOne { it.code eq tenantCode }
            ?: throw IllegalArgumentException("Tenant not found: $tenantCode")
        assignUserToTenant(userId, tenant.id)
    }

    @Transactional
    override fun assignUserToTenant(userId: Int, tenantId: Int) {
        // Check if tenant exists
        val tenant = tenantDao.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")

        // Check if already assigned
        val existing = userTenantDao.findByUserIdAndTenantId(userId, tenantId)
        if (existing != null) {
            logger.warn("User {} is already assigned to tenant {} (ID: {})", userId, tenant.code, tenantId)
            return
        }

        // Create assignment
        val userTenant = UserTenant()
        userTenant.userId = userId
        userTenant.tenantId = tenantId
        userTenant.createdAt = Instant.now()
        userTenantDao.add(userTenant)
        logger.info("Successfully assigned user {} to tenant {} (ID: {})", userId, tenant.code, tenantId)
    }

    override fun getTenantsForUser(userId: Int): List<TenantDto> {
        val tenantIds = userTenantDao.getTenantIdsByUserId(userId)
        if (tenantIds.isEmpty()) return emptyList()
        return tenantDao.findByIds(tenantIds).map { it.toDto() }
    }

    override fun findTenantByEmail(email: String): TenantDto? {
        val emailDomain = EmailDomainMatcher.extractDomain(email)
        if (emailDomain == null) {
            logger.debug("Invalid email format: {}", email)
            return null
        }

        // Get all active tenants
        val tenants = tenantDao.findList { it.status eq 1 }

        // Find matching tenant
        return tenants.firstOrNull { tenant ->
            EmailDomainMatcher.matchesDomain(emailDomain, tenant.emailDomains ?: "")
        }?.toDto()
    }

    override fun isEmailMatchesTenant(email: String, tenantId: Int): Boolean {
        val tenant = tenantDao.findById(tenantId) ?: return false
        return EmailDomainMatcher.matches(email, tenant.emailDomains ?: "")
    }
}
