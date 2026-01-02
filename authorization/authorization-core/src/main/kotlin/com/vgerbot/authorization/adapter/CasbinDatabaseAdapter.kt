package com.vgerbot.authorization.adapter

import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.Adapter
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.sql.DataSource

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
 */
class CasbinDatabaseAdapter(
    private val database: Database,
    private val dataSource: DataSource
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
        
        dataSource.connection.use { conn ->
            // 加载策略规则 (p)：角色-权限映射 (RBAC)
            loadPoliciesFromRolePermission(conn, model)
            
            // 加载 ACL 策略规则 (p)：用户-权限直接映射
            loadPoliciesFromUserPermission(conn, model)
            
            // 加载分组规则 (g)：用户-角色映射
            loadGroupingFromUserRole(conn, model)
            
            // 加载多租户分组规则（如果启用）
            loadGroupingFromUserTenant(conn, model)
        }
        
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
     * 格式：p, role_code, resource, action [, domain]
     */
    private fun loadPoliciesFromRolePermission(conn: Connection, model: Model) {
        val sql = """
            SELECT r.code as role_code, p.resource, p.action, t.id as tenant_id
            FROM role_permission rp
            INNER JOIN role r ON rp.role_id = r.id AND r.status = 1
            INNER JOIN permission p ON rp.permission_id = p.id AND p.status = 1
            LEFT JOIN user_role ur ON ur.role_id = r.id AND ur.is_deleted = 0
            LEFT JOIN user_tenant ut ON ut.user_id = ur.user_id AND ut.is_deleted = 0
            LEFT JOIN tenant t ON t.id = ut.tenant_id AND t.is_deleted = 0
            WHERE rp.is_deleted = 0
            GROUP BY r.code, p.resource, p.action, t.id
        """.trimIndent()
        
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val roleCode = rs.getString("role_code")
                    val resource = rs.getString("resource")
                    val action = rs.getString("action")
                    val tenantId = rs.getString("tenant_id")
                    
                    // 如果有租户信息，使用带域的策略
                    if (tenantId != null) {
                        // p, role, domain, resource, action
                        model.addPolicy("p", "p", listOf(roleCode, tenantId, resource, action))
                    } else {
                        // p, role, resource, action
                        model.addPolicy("p", "p", listOf(roleCode, resource, action))
                    }
                }
            }
        }
    }
    
    /**
     * 从 user_permission 表加载策略（ACL 模式）
     * 格式：p, user_id, resource, action [, domain]
     * 
     * ACL 模式下，用户直接拥有权限，不需要通过角色
     */
    private fun loadPoliciesFromUserPermission(conn: Connection, model: Model) {
        val sql = """
            SELECT up.user_id, p.resource, p.action, up.tenant_id
            FROM user_permission up
            INNER JOIN permission p ON up.permission_id = p.id AND p.status = 1
            WHERE up.is_deleted = 0
        """.trimIndent()
        
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val userId = rs.getInt("user_id").toString()
                    val resource = rs.getString("resource")
                    val action = rs.getString("action")
                    val tenantId = rs.getString("tenant_id")
                    
                    // 如果有租户信息，使用带域的策略
                    if (tenantId != null) {
                        // p, user, domain, resource, action
                        model.addPolicy("p", "p", listOf(userId, tenantId, resource, action))
                    } else {
                        // p, user, resource, action
                        model.addPolicy("p", "p", listOf(userId, resource, action))
                    }
                    
                    logger.debug("Loaded ACL policy: user={}, resource={}, action={}, tenant={}", 
                        userId, resource, action, tenantId)
                }
            }
        }
    }
    
    /**
     * 从 user_role 表加载分组规则
     * 格式：g, user_id, role_code [, domain]
     */
    private fun loadGroupingFromUserRole(conn: Connection, model: Model) {
        val sql = """
            SELECT ur.user_id, r.code as role_code, t.id as tenant_id
            FROM user_role ur
            INNER JOIN role r ON ur.role_id = r.id AND r.status = 1
            LEFT JOIN user_tenant ut ON ut.user_id = ur.user_id AND ut.is_deleted = 0
            LEFT JOIN tenant t ON t.id = ut.tenant_id AND t.is_deleted = 0
            WHERE ur.is_deleted = 0
        """.trimIndent()
        
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val userId = rs.getInt("user_id").toString()
                    val roleCode = rs.getString("role_code")
                    val tenantId = rs.getString("tenant_id")
                    
                    // 如果有租户信息，使用带域的分组
                    if (tenantId != null) {
                        // g, user, role, domain
                        model.addPolicy("g", "g", listOf(userId, roleCode, tenantId))
                    } else {
                        // g, user, role
                        model.addPolicy("g", "g", listOf(userId, roleCode))
                    }
                }
            }
        }
    }
    
    /**
     * 从 user_tenant 表加载租户分组规则
     * 这个方法用于支持多租户场景
     */
    private fun loadGroupingFromUserTenant(conn: Connection, model: Model) {
        val sql = """
            SELECT ut.user_id, t.id as tenant_id, t.code as tenant_code
            FROM user_tenant ut
            INNER JOIN tenant t ON ut.tenant_id = t.id AND t.is_deleted = 0
            WHERE ut.is_deleted = 0
        """.trimIndent()
        
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val userId = rs.getInt("user_id").toString()
                    val tenantId = rs.getInt("tenant_id").toString()
                    val tenantCode = rs.getString("tenant_code")
                    
                    // 添加用户-租户关系（用于域隔离）
                    // 这个信息可以在权限检查时使用
                    logger.debug("User {} belongs to tenant {} ({})", userId, tenantId, tenantCode)
                }
            }
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
