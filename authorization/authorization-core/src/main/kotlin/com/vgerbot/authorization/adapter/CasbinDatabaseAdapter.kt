package com.vgerbot.authorization.adapter

import com.vgerbot.authorization.dao.CasbinPolicyDao
import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.Adapter
import org.slf4j.LoggerFactory

/**
 * Casbin 数据库适配器
 * 使用现有的数据库表来存储 Casbin 策略
 * 
 * 该适配器会使用以下数据表：
 * - user: 用户表
 * - role: 角色表
 * - permission: 权限表
 * - user_role: 用户角色关联表
 * - role_permission: 角色权限关联表
 * - user_permission: 用户权限关联表（ACL 支持）
 * - tenant: 租户表
 * - user_tenant: 用户租户关联表
 * 
 * @param casbinPolicyDao 策略数据访问对象
 * @param useDomains 是否使用域模式（多租户），决定策略参数数量
 */
class CasbinDatabaseAdapter(
    private val casbinPolicyDao: CasbinPolicyDao,
    private val useDomains: Boolean = false
) : Adapter {
    
    private val logger = LoggerFactory.getLogger(CasbinDatabaseAdapter::class.java)
    
    /**
     * 从数据库加载所有策略
     * 这个方法会从现有的 RBAC 表中读取数据并转换为 Casbin 策略格式
     */
    override fun loadPolicy(model: Model?) {
        if (model == null) {
            logger.warn("Model is null, skipping policy loading")
            return
        }
        
        logger.info("Loading policies from database...")
        
        // 加载策略规则 (p)：角色-权限映射 (RBAC)
        loadPoliciesFromRolePermission(model)
        
        // 加载 ACL 策略规则 (p)：用户-权限直接映射
        loadPoliciesFromUserPermission(model)
        
        // 加载分组规则 (g)：用户-角色映射
        loadGroupingFromUserRole(model)
        
        // 加载多租户分组规则（如果启用）
        loadGroupingFromUserTenant(model)
        
        logger.info("Policies loaded successfully")
    }
    
    /**
     * 保存所有策略到数据库
     * 注意：这个方法不建议直接使用，建议通过 RBAC 模块的 API 来管理策略
     */
    override fun savePolicy(model: Model?) {
        if (model == null) {
            logger.warn("Model is null, skipping policy saving")
            return
        }
        logger.warn("Direct save policy is not recommended. Please use RBAC module APIs to manage policies.")
    }
    
    /**
     * 从 role_permission 表加载策略（RBAC 模式）
     * 格式：
     * - 域模式: p, role_code, domain, resource, action (4个参数)
     * - 非域模式: p, role_code, resource, action (3个参数)
     */
    private fun loadPoliciesFromRolePermission(model: Model) {
        val policies = casbinPolicyDao.loadRolePermissionPolicies()
        
        policies.forEach { policy ->
            if (useDomains) {
                // 域模式：始终使用4个参数，无租户时使用通配符 "*"
                val domain = policy.tenantId?.toString() ?: "*"
                model.addPolicy("p", "p", listOf(policy.roleCode, domain, policy.resource, policy.action))
            } else {
                // 非域模式：始终使用3个参数，忽略租户信息
                model.addPolicy("p", "p", listOf(policy.roleCode, policy.resource, policy.action))
            }
        }
    }
    
    /**
     * 从 user_permission 表加载策略（ACL 模式）
     * 格式：
     * - 域模式: p, user_id, domain, resource, action (4个参数)
     * - 非域模式: p, user_id, resource, action (3个参数)
     * 
     * ACL 模式下，用户直接拥有权限，不需要通过角色
     */
    private fun loadPoliciesFromUserPermission(model: Model) {
        val policies = casbinPolicyDao.loadUserPermissionPolicies()
        
        policies.forEach { policy ->
            val userId = policy.userId.toString()
            
            if (useDomains) {
                // 域模式：始终使用4个参数，无租户时使用通配符 "*"
                val domain = policy.tenantId?.toString() ?: "*"
                model.addPolicy("p", "p", listOf(userId, domain, policy.resource, policy.action))
                logger.debug("Loaded ACL policy (domain mode): user={}, domain={}, resource={}, action={}", 
                    userId, domain, policy.resource, policy.action)
            } else {
                // 非域模式：始终使用3个参数，忽略租户信息
                model.addPolicy("p", "p", listOf(userId, policy.resource, policy.action))
                logger.debug("Loaded ACL policy: user={}, resource={}, action={}", 
                    userId, policy.resource, policy.action)
            }
        }
    }
    
    /**
     * 从 user_role 表加载分组规则
     * 格式：
     * - 域模式: g, user_id, role_code, domain (3个参数)
     * - 非域模式: g, user_id, role_code (2个参数)
     * 
     * 注意：参数数量必须与模型定义一致，否则会报错：
     * "grouping policy elements do not meet role definition"
     */
    private fun loadGroupingFromUserRole(model: Model) {
        val groupings = casbinPolicyDao.loadUserRoleGroupings()
        
        groupings.forEach { grouping ->
            val userId = grouping.userId.toString()
            
            if (useDomains) {
                // 域模式：始终使用3个参数，无租户时使用通配符 "*"
                val domain = grouping.tenantId?.toString() ?: "*"
                model.addPolicy("g", "g", listOf(userId, grouping.roleCode, domain))
                logger.debug("Loaded grouping (domain mode): user={}, role={}, domain={}", 
                    userId, grouping.roleCode, domain)
            } else {
                // 非域模式：始终使用2个参数，忽略租户信息
                model.addPolicy("g", "g", listOf(userId, grouping.roleCode))
                logger.debug("Loaded grouping: user={}, role={}", userId, grouping.roleCode)
            }
        }
    }
    
    /**
     * 从 user_tenant 表加载租户分组规则
     * 这个方法用于支持多租户场景
     */
    private fun loadGroupingFromUserTenant(@Suppress("UNUSED_PARAMETER") model: Model) {
        val groupings = casbinPolicyDao.loadUserTenantGroupings()
        
        groupings.forEach { grouping ->
            val userId = grouping.userId.toString()
            val tenantId = grouping.tenantId.toString()
            
            // 添加用户-租户关系（用于域隔离）
            // 这个信息可以在权限检查时使用
            logger.debug("User {} belongs to tenant {} ({})", userId, tenantId, grouping.tenantCode)
        }
    }
    
    /**
     * 添加单个策略
     */
    override fun addPolicy(sec: String, ptype: String, rule: List<String>) {
        logger.info("Adding policy: {} {} {}", sec, ptype, rule)
        // 实际的策略添加应该通过 RBAC 模块的 API 来完成
        // 这里只是记录日志
    }
    
    /**
     * 移除单个策略
     */
    override fun removePolicy(sec: String, ptype: String, rule: List<String>) {
        logger.info("Removing policy: {} {} {}", sec, ptype, rule)
        // 实际的策略移除应该通过 RBAC 模块的 API 来完成
        // 这里只是记录日志
    }
    
    /**
     * 移除过滤后的策略
     */
    override fun removeFilteredPolicy(sec: String, ptype: String, fieldIndex: Int, vararg fieldValues: String) {
        logger.info("Removing filtered policy: {} {} {} {}", sec, ptype, fieldIndex, fieldValues.joinToString())
    }
}
