package com.vgerbot.authorization.service

import com.vgerbot.authorization.adapter.CasbinDatabaseAdapter
import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.authorization.api.Permission
import com.vgerbot.authorization.api.PermissionRequest
import com.vgerbot.authorization.api.PolicyType
import com.vgerbot.authorization.config.AuthorizationProperties
import org.casbin.jcasbin.main.Enforcer
import org.casbin.jcasbin.model.Model
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.sql.DataSource

/**
 * Casbin 授权服务实现
 */
@Service
class CasbinAuthorizationService(
    private val database: Database,
    private val dataSource: DataSource,
    private val properties: AuthorizationProperties
) : AuthorizationService {
    
    private val logger = LoggerFactory.getLogger(CasbinAuthorizationService::class.java)
    private lateinit var enforcer: Enforcer
    
    init {
        logger.info("Initializing Casbin Authorization Service with policy type: {}", properties.policyType)
        
        // 加载模型配置
        val model = loadModel()
        
        // 创建适配器
        val adapter = if (properties.useDatabaseAdapter) {
            CasbinDatabaseAdapter(database, dataSource)
        } else {
            null
        }
        
        // 创建 Enforcer
        enforcer = if (adapter != null) {
            Enforcer(model, adapter)
        } else {
            Enforcer(model)
        }
        
        // 设置自动保存
        enforcer.enableAutoSave(properties.autoSave)
        
        logger.info("Casbin Authorization Service initialized successfully")
    }
    
    /**
     * 加载模型配置
     */
    private fun loadModel(): Model {
        val modelPath = properties.customModelPath ?: getDefaultModelPath()
        logger.info("Loading Casbin model from: {}", modelPath)
        
        // 从 classpath 加载模型
        val modelText = javaClass.classLoader.getResourceAsStream(modelPath)?.bufferedReader()?.readText()
            ?: throw IllegalStateException("Cannot load Casbin model from: $modelPath")
        
        val model = Model()
        model.loadModelFromText(modelText)
        return model
    }
    
    /**
     * 获取默认模型路径
     */
    private fun getDefaultModelPath(): String {
        return when (properties.policyType) {
            PolicyType.RBAC -> "casbin/rbac_model.conf"
            PolicyType.ACL -> "casbin/acl_model.conf"
            PolicyType.ABAC -> "casbin/abac_model.conf"
            PolicyType.RBAC_WITH_DOMAINS -> "casbin/rbac_with_domains_model.conf"
        }
    }
    
    override fun enforce(subject: String, resource: String, action: String, domain: String?): Boolean {
        return try {
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                enforcer.enforce(subject, domain, resource, action)
            } else {
                enforcer.enforce(subject, resource, action)
            }
            logger.debug("Enforce: subject={}, resource={}, action={}, domain={}, result={}", 
                subject, resource, action, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error during enforce check", e)
            false
        }
    }
    
    override fun batchEnforce(requests: List<PermissionRequest>): List<Boolean> {
        return requests.map { request ->
            enforce(request.subject, request.resource, request.action, request.domain)
        }
    }
    
    override fun addPolicy(subject: String, resource: String, action: String, domain: String?): Boolean {
        return try {
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                enforcer.addPolicy(subject, domain, resource, action)
            } else {
                enforcer.addPolicy(subject, resource, action)
            }
            logger.info("Policy added: subject={}, resource={}, action={}, domain={}, result={}", 
                subject, resource, action, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error adding policy", e)
            false
        }
    }
    
    override fun removePolicy(subject: String, resource: String, action: String, domain: String?): Boolean {
        return try {
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                enforcer.removePolicy(subject, domain, resource, action)
            } else {
                enforcer.removePolicy(subject, resource, action)
            }
            logger.info("Policy removed: subject={}, resource={}, action={}, domain={}, result={}", 
                subject, resource, action, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error removing policy", e)
            false
        }
    }
    
    override fun getPermissionsForSubject(subject: String, domain: String?): List<Permission> {
        return try {
            val permissions = enforcer.getPermissionsForUser(subject)
            permissions.map { policy ->
                when {
                    policy.size >= 4 && domain != null -> 
                        Permission(policy[0], policy[2], policy[3], policy[1])
                    policy.size >= 3 -> 
                        Permission(policy[0], policy[1], policy[2])
                    else -> null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            logger.error("Error getting permissions for subject", e)
            emptyList()
        }
    }
    
    override fun getPermissionsForResource(resource: String, domain: String?): List<Permission> {
        return try {
            val allPolicies = enforcer.getPolicy()
            allPolicies.filter { policy ->
                when {
                    domain != null && policy.size >= 4 -> policy[1] == domain && policy[2] == resource
                    policy.size >= 3 -> policy[1] == resource
                    else -> false
                }
            }.map { policy ->
                when {
                    policy.size >= 4 && domain != null -> 
                        Permission(policy[0], policy[2], policy[3], policy[1])
                    policy.size >= 3 -> 
                        Permission(policy[0], policy[1], policy[2])
                    else -> null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            logger.error("Error getting permissions for resource", e)
            emptyList()
        }
    }
    
    override fun addRoleInheritance(role: String, parentRole: String, domain: String?): Boolean {
        return try {
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                // 使用分组策略手动添加域支持
                enforcer.addGroupingPolicy(role, parentRole, domain)
            } else {
                enforcer.addRoleForUser(role, parentRole)
            }
            logger.info("Role inheritance added: role={}, parent={}, domain={}, result={}", 
                role, parentRole, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error adding role inheritance", e)
            false
        }
    }
    
    override fun removeRoleInheritance(role: String, parentRole: String, domain: String?): Boolean {
        return try {
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                // 使用分组策略手动删除域支持
                enforcer.removeGroupingPolicy(role, parentRole, domain)
            } else {
                enforcer.deleteRoleForUser(role, parentRole)
            }
            logger.info("Role inheritance removed: role={}, parent={}, domain={}, result={}", 
                role, parentRole, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error removing role inheritance", e)
            false
        }
    }
    
    override fun getRolesForUser(userId: Int, domain: String?): List<String> {
        return try {
            val userIdStr = userId.toString()
            if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                // 手动从分组策略中获取
                enforcer.getFilteredGroupingPolicy(0, userIdStr, domain).map { it[1] }
            } else {
                enforcer.getRolesForUser(userIdStr).toList()
            }
        } catch (e: Exception) {
            logger.error("Error getting roles for user", e)
            emptyList()
        }
    }
    
    override fun getUsersForRole(role: String, domain: String?): List<Int> {
        return try {
            val users = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                // 手动从分组策略中获取
                enforcer.getFilteredGroupingPolicy(1, role, domain).map { it[0] }
            } else {
                enforcer.getUsersForRole(role).toList()
            }
            users.mapNotNull { it.toIntOrNull() }
        } catch (e: Exception) {
            logger.error("Error getting users for role", e)
            emptyList()
        }
    }
    
    override fun addRoleForUser(userId: Int, role: String, domain: String?): Boolean {
        return try {
            val userIdStr = userId.toString()
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                enforcer.addGroupingPolicy(userIdStr, role, domain)
            } else {
                enforcer.addRoleForUser(userIdStr, role)
            }
            logger.info("Role added for user: userId={}, role={}, domain={}, result={}", 
                userId, role, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error adding role for user", e)
            false
        }
    }
    
    override fun removeRoleForUser(userId: Int, role: String, domain: String?): Boolean {
        return try {
            val userIdStr = userId.toString()
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                enforcer.removeGroupingPolicy(userIdStr, role, domain)
            } else {
                enforcer.deleteRoleForUser(userIdStr, role)
            }
            logger.info("Role removed for user: userId={}, role={}, domain={}, result={}", 
                userId, role, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error removing role for user", e)
            false
        }
    }
    
    override fun deleteRolesForUser(userId: Int, domain: String?): Boolean {
        return try {
            val userIdStr = userId.toString()
            val result = if (domain != null && properties.policyType == PolicyType.RBAC_WITH_DOMAINS) {
                // 删除所有与用户相关的分组策略
                enforcer.removeFilteredGroupingPolicy(0, userIdStr, domain)
            } else {
                enforcer.deleteRolesForUser(userIdStr)
            }
            logger.info("All roles deleted for user: userId={}, domain={}, result={}", 
                userId, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error deleting roles for user", e)
            false
        }
    }
    
    override fun deleteRole(role: String, domain: String?): Boolean {
        return try {
            // 删除所有与该角色相关的分组规则
            enforcer.deleteRole(role)
            logger.info("Role deleted: role={}, domain={}", role, domain)
            true
        } catch (e: Exception) {
            logger.error("Error deleting role", e)
            false
        }
    }
    
    override fun deletePermission(resource: String, action: String, domain: String?): Boolean {
        return try {
            val result = enforcer.removeFilteredPolicy(1, resource, action)
            logger.info("Permission deleted: resource={}, action={}, domain={}, result={}", 
                resource, action, domain, result)
            result
        } catch (e: Exception) {
            logger.error("Error deleting permission", e)
            false
        }
    }
    
    override fun reloadPolicy() {
        try {
            enforcer.loadPolicy()
            logger.info("Policy reloaded successfully")
        } catch (e: Exception) {
            logger.error("Error reloading policy", e)
        }
    }
}

