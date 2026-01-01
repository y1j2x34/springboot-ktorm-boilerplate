package com.vgerbot.tenant.service

import com.vgerbot.tenant.com.vgerbot.tenant.dto.TenantInfo
import com.vgerbot.tenant.com.vgerbot.tenant.service.TenantService
import com.vgerbot.tenant.dao.TenantDao
import com.vgerbot.tenant.dao.UserTenantDao
import com.vgerbot.tenant.entity.toDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 租户服务
 *
 * 提供租户信息查询和用户租户关联管理
 */
@Service
class TenantServiceImpl: TenantService {

    private val logger = LoggerFactory.getLogger(TenantServiceImpl::class.java)

    @Autowired
    private lateinit var tenantDao: TenantDao

    @Autowired
    private lateinit var userTenantDao: UserTenantDao
    override fun getTenantByCode(code: String): TenantInfo? {
        TODO("Not yet implemented")
    }
    override fun getTenantById(id: Int): TenantInfo? = tenantDao.findById(id)?.toDto()


    override fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun assignUserToTenant(userId: Int, tenantCode: String) {
        TODO("Not yet implemented")
    }

    override fun assignUserToTenant(userId: Int, tenantId: Int) {
        TODO("Not yet implemented")
    }

    override fun findTenantByEmail(email: String): TenantInfo? {
        TODO("Not yet implemented")
    }

    override fun isEmailMatchesTenant(email: String, tenantId: Int): Boolean {
        TODO("Not yet implemented")
    }


//
//    /**
//     * 根据用户 ID 获取租户信息（返回第一个关联的租户）
//     */
//    fun getDefaultTenantForUser(userId: Int): TenantInfo? {
//        val userTenant = userTenantDao.findDefaultByUserId(userId)
//        if (userTenant == null) {
//            logger.debug("User {} has no tenant", userId)
//            return null
//        }
//
//        return tenantDao.findById(userTenant.tenantId)
//    }
//
//    /**
//     * 根据租户 ID 获取租户信息
//     */
//    fun getTenantById(tenantId: Int): Tenant? {
//        return tenantDao.findById(tenantId)
//    }
//
//    /**
//     * 检查用户是否属于指定租户
//     */
//    fun isUserBelongsToTenant(userId: Int, tenantId: Int): Boolean {
//        return userTenantDao.findByUserIdAndTenantId(userId, tenantId) != null
//    }
//
//    /**
//     * 将用户分配到租户
//     *
//     * @param userId 用户ID
//     * @param tenantCode 租户代码
//     * @throws IllegalArgumentException 如果租户不存在
//     * @throws IllegalStateException 如果用户已经绑定到该租户
//     */
//    @Transactional
//    fun assignUserToTenant(userId: Int, tenantCode: String) {
//        // 查找租户
//        val tenant = tenantDao.findByCode(tenantCode)
//            ?: throw IllegalArgumentException("租户不存在: $tenantCode")
//
//        // 检查是否已经关联
//        val existing = userTenantDao.findByUserIdAndTenantId(userId, tenant.id)
//        if (existing != null) {
//            logger.warn("用户 {} 已经绑定到租户 {} (ID: {})", userId, tenantCode, tenant.id)
//            return
//        }
//
//        // 创建关联
//        userTenantDao.create(userId, tenant.id)
//        logger.info("成功将用户 {} 分配到租户 {} (ID: {})", userId, tenantCode, tenant.id)
//    }
//
//    /**
//     * 将用户分配到租户（通过租户ID）
//     *
//     * @param userId 用户ID
//     * @param tenantId 租户ID
//     */
//    @Transactional
//    fun assignUserToTenantById(userId: Int, tenantId: Int) {
//        // 检查租户是否存在
//        val tenant = tenantDao.findById(tenantId)
//            ?: throw IllegalArgumentException("租户不存在: $tenantId")
//
//        // 检查是否已经关联
//        val existing = userTenantDao.findByUserIdAndTenantId(userId, tenantId)
//        if (existing != null) {
//            logger.warn("用户 {} 已经绑定到租户 {} (ID: {})", userId, tenant.code, tenantId)
//            return
//        }
//
//        // 创建关联
//        userTenantDao.create(userId, tenantId)
//        logger.info("成功将用户 {} 分配到租户 {} (ID: {})", userId, tenant.code, tenantId)
//    }
//
//    /**
//     * 根据邮箱查找匹配的租户
//     *
//     * @param email 用户邮箱
//     * @return 匹配的租户列表
//     */
//    fun findTenantsByEmail(email: String): List<Tenant> {
//        // 提取邮箱域名
//        val emailDomain = EmailDomainMatcher.extractDomain(email)
//        if (emailDomain == null) {
//            logger.debug("Invalid email format: {}", email)
//            return emptyList()
//        }
//
//        // 获取所有激活的租户
//        val tenants = tenantDao.findAllActive()
//
//        // 使用 EmailDomainMatcher 工具类过滤出匹配的租户
//        return tenants.filter { tenant ->
//            EmailDomainMatcher.matchesDomain(emailDomain, tenant.emailDomains)
//        }
//    }
//
//    /**
//     * 检查邮箱是否匹配租户配置的域名模式
//     *
//     * 支持的模式：
//     * 1. 精确匹配：example.com
//     * 2. 大括号扩展：comp.{com,cn} -> [comp.com, comp.cn]
//     * 3. 多个域名用逗号分隔：example.com,test.com
//     * 4. 通配符：*.example.com -> 匹配任意子域名
//     *
//     * @param email 邮箱地址
//     * @param tenantId 租户ID
//     * @return 是否匹配
//     */
//    fun isEmailMatchesTenant(email: String, tenantId: Int): Boolean {
//        val tenant = tenantDao.findById(tenantId) ?: return false
//        return EmailDomainMatcher.matches(email, tenant.emailDomains)
//    }
}