package com.vgerbot.tenant.com.vgerbot.tenant.context

import org.slf4j.LoggerFactory

/**
 * 租户上下文持有者
 * 
 * 使用 ThreadLocal 存储当前请求的租户信息
 * 用于在应用程序的任何地方获取当前租户信息
 */
object TenantContextHolder {
    
    private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
    
    private val tenantContext = ThreadLocal<TenantContext>()
    
    /**
     * 设置租户上下文
     */
    fun setContext(context: TenantContext) {
        tenantContext.set(context)
        logger.debug("Set tenant context: {}", context)
    }
    
    /**
     * 获取租户上下文
     */
    fun getContext(): TenantContext? {
        return tenantContext.get()
    }
    
    /**
     * 获取当前租户 ID
     */
    fun getTenantId(): Int? {
        return tenantContext.get()?.tenantId
    }
    
    /**
     * 获取当前租户代码
     */
    fun getTenantCode(): String? {
        return tenantContext.get()?.tenantCode
    }
    
    /**
     * 获取当前租户名称
     */
    fun getTenantName(): String? {
        return tenantContext.get()?.tenantName
    }
    
    /**
     * 清除租户上下文
     */
    fun clear() {
        tenantContext.remove()
        logger.debug("Cleared tenant context")
    }
}

/**
 * 租户上下文数据类
 */
data class TenantContext(
    val tenantId: Int?,
    val tenantCode: String?,
    val tenantName: String?
)

