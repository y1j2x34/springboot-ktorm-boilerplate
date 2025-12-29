package com.vgerbot.com.vgerbot.tenant.listener

import com.vgerbot.common.event.UserCreatedEvent
import com.vgerbot.tenant.com.vgerbot.tenant.context.TenantContextHolder
import com.vgerbot.tenant.com.vgerbot.tenant.service.TenantService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 用户事件监听器
 *
 * 监听用户模块发布的事件，在用户创建时自动执行租户相关的初始化操作
 * 这种方式保持了模块隔离性：
 * - user 模块不需要知道 tenant 模块的存在
 * - tenant 模块通过事件机制响应用户操作
 * - 可以轻松添加更多的监听器而不影响 user 模块
 *
 * 事务处理：
 * - 使用 @TransactionalEventListener(phase = BEFORE_COMMIT)
 * - 在同一事务中执行，保证原子性：要么全部成功，要么全部失败
 * - 如果租户绑定失败，整个事务（包括用户创建）都会回滚
 */
@Component
class UserEventListener(
    private val tenantService: TenantService
) {
    private val logger = LoggerFactory.getLogger(UserEventListener::class.java)

    /**
     * 监听用户创建事件
     *
     * 当新用户被创建时，自动根据邮箱域名将其绑定到匹配的租户
     *
     * @TransactionalEventListener(phase = BEFORE_COMMIT)
     * - 在事务提交前执行，与用户创建在同一个事务中
     * - 如果此方法抛出异常，整个事务会回滚
     * - 保证数据一致性：用户创建和租户绑定要么都成功，要么都失败
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleUserCreated(event: UserCreatedEvent) {
        logger.info("收到用户创建事件: userId={}, username={}, email={}",
            event.userId, event.username, event.email)

        try {
            // 1. 首先尝试从上下文获取租户（例如通过注册链接指定的租户）
            val contextTenantCode = TenantContextHolder.getTenantCode()
            if (contextTenantCode != null) {
                logger.info("从上下文中获取到租户代码: {}", contextTenantCode)
                tenantService.assignUserToTenant(event.userId, contextTenantCode)
                logger.info("成功将用户 {} (ID: {}) 绑定到上下文租户 {}",
                    event.username, event.userId, contextTenantCode)
                return
            }

            // 2. 根据邮箱域名自动匹配租户
            val matchedTenant = tenantService.findTenantByEmail(event.email)

            matchedTenant?.let {
                tenant ->
                logger.info("为邮箱 {} 找到唯一匹配租户: {} ({})",
                    event.email, tenant.name, tenant.code)
                tenantService.assignUserToTenant(event.userId, tenant.code)
                logger.info("成功将用户 {} (ID: {}) 自动绑定到租户 {} ({})",
                    event.username, event.userId, tenant.code, tenant.name)
            } ?: {
                val defaultTenantCode = getDefaultTenantCode()
                logger.info("未找到匹配邮箱 {} 的租户，使用默认租户: {}",
                    event.email, defaultTenantCode)
                tenantService.assignUserToTenant(event.userId, defaultTenantCode)
                logger.info("成功将用户 {} (ID: {}) 绑定到默认租户 {}",
                    event.username, event.userId, defaultTenantCode)
            }
        } catch (e: Exception) {
            logger.error("处理用户创建事件失败: userId={}, email={}",
                event.userId, event.email, e)
            throw e // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 获取默认租户代码
     *
     * 当没有匹配的租户时使用
     * 可以从配置文件读取，或硬编码一个默认值
     */
    private fun getDefaultTenantCode(): String {
        // TODO: 可以从字典表中读取默认租户配置
        // @Value("\${tenant.default-code:tenant_demo}")
        // private lateinit var defaultTenantCode: String
        return "tenant_demo"
    }
}