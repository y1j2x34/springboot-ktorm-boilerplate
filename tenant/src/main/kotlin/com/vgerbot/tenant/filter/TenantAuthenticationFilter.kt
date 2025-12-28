package com.vgerbot.tenant.filter

import com.vgerbot.common.user.UserService
import com.vgerbot.tenant.context.TenantContext
import com.vgerbot.tenant.context.TenantContextHolder
import com.vgerbot.tenant.security.TenantAuthenticationToken
import com.vgerbot.tenant.security.TenantPrincipal
import com.vgerbot.tenant.service.TenantService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 租户认证过滤器
 * 
 * 此过滤器在 JWT 认证之后执行，将租户信息注入到 Spring Security 的 Authentication 对象中
 * 
 * 工作流程：
 * 1. 从 Security Context 获取已认证的用户信息
 * 2. 根据用户名查询用户 ID
 * 3. 查询用户的默认租户（可扩展为从请求头获取租户 ID）
 * 4. 将 UserDetails 包装成 TenantPrincipal
 * 5. 创建 TenantAuthenticationToken 替换原有的 Authentication
 * 6. 将租户信息也存入 ThreadLocal（TenantContextHolder）
 */
@Component
class TenantAuthenticationFilter : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(TenantAuthenticationFilter::class.java)
    
    @Autowired
    private lateinit var tenantService: TenantService
    
    @Autowired
    private lateinit var userService: UserService
    
    companion object {
        /**
         * 请求头中的租户 ID 字段名
         * 如果请求头中指定了租户 ID，则使用该租户（需要验证用户是否属于该租户）
         */
        const val TENANT_HEADER = "X-Tenant-Id"
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            
            // 只处理已认证的用户
            if (authentication != null && authentication.isAuthenticated 
                && authentication.principal != "anonymousUser") {
                
                // 如果已经是 TenantAuthenticationToken，跳过处理
                if (authentication is TenantAuthenticationToken) {
                    logger.debug("Authentication already contains tenant info, skipping")
                    setTenantContext(authentication.tenantId, authentication.tenantCode, authentication.tenantName)
                    filterChain.doFilter(request, response)
                    return
                }
                
                // 获取用户信息
                val principal = authentication.principal
                if (principal is UserDetails) {
                    val username = principal.username
                    
                    // 查询用户 ID
                    val user = userService.findUser(username)
                    if (user != null) {
                        val userId = user.id
                        
                        // 获取租户信息
                        val tenant = getTenantForUser(userId, request)
                        
                        if (tenant != null) {
                            logger.debug("Found tenant for user {}: {}", username, tenant.name)
                            
                            // 包装 Principal
                            val tenantPrincipal = TenantPrincipal(
                                delegate = principal,
                                tenantId = tenant.id,
                                tenantCode = tenant.code,
                                tenantName = tenant.name
                            )
                            
                            // 创建新的 Authentication Token
                            val tenantAuth = TenantAuthenticationToken(
                                tenantPrincipal,
                                authentication.credentials,
                                authentication.authorities
                            )
                            
                            // 复制原有的 details
                            tenantAuth.details = authentication.details
                            
                            // 替换 Security Context 中的 Authentication
                            SecurityContextHolder.getContext().authentication = tenantAuth
                            
                            // 设置 ThreadLocal 上下文
                            setTenantContext(tenant.id, tenant.code, tenant.name)
                            
                            logger.debug("Successfully injected tenant info into authentication for user: {}", username)
                        } else {
                            logger.debug("No tenant found for user: {}", username)
                            // 即使没有租户信息，也设置一个空的上下文
                            setTenantContext(null, null, null)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing tenant authentication", e)
            // 出错时不中断请求，只是记录日志
        }
        
        try {
            filterChain.doFilter(request, response)
        } finally {
            // 清除 ThreadLocal，避免内存泄漏
            TenantContextHolder.clear()
        }
    }
    
    /**
     * 获取用户的租户信息
     * 
     * 策略：
     * 1. 如果请求头中指定了租户 ID，且用户属于该租户，则使用指定的租户
     * 2. 否则使用用户的默认租户
     */
    private fun getTenantForUser(userId: Int, request: HttpServletRequest): com.vgerbot.tenant.model.Tenant? {
        // 1. 尝试从请求头获取租户 ID
        val tenantIdHeader = request.getHeader(TENANT_HEADER)
        if (tenantIdHeader != null) {
            val requestedTenantId = tenantIdHeader.toIntOrNull()
            if (requestedTenantId != null) {
                // 验证用户是否属于该租户
                if (tenantService.isUserBelongsToTenant(userId, requestedTenantId)) {
                    val tenant = tenantService.getTenantById(requestedTenantId)
                    if (tenant != null) {
                        logger.debug("Using tenant from header: {}", requestedTenantId)
                        return tenant
                    }
                } else {
                    logger.warn("User {} does not belong to tenant {}", userId, requestedTenantId)
                }
            }
        }
        
        // 2. 使用默认租户
        return tenantService.getDefaultTenantForUser(userId)
    }
    
    /**
     * 设置租户上下文到 ThreadLocal
     */
    private fun setTenantContext(tenantId: Int?, tenantCode: String?, tenantName: String?) {
        val context = TenantContext(tenantId, tenantCode, tenantName)
        TenantContextHolder.setContext(context)
    }
}

