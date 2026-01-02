package com.vgerbot.authorization.config

import com.vgerbot.authorization.api.PolicyType
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Authorization 模块配置属性
 */
@ConfigurationProperties(prefix = "authorization")
data class AuthorizationProperties(
    /**
     * 是否启用授权模块
     */
    var enabled: Boolean = true,
    
    /**
     * 策略类型
     * 支持: RBAC, ACL, ABAC, RBAC_WITH_DOMAINS
     */
    var policyType: PolicyType = PolicyType.RBAC_WITH_DOMAINS,
    
    /**
     * 自定义模型配置文件路径（可选）
     * 如果指定，将使用自定义配置文件而不是内置的模型
     */
    var customModelPath: String? = null,
    
    /**
     * 是否使用数据库适配器
     * true: 从数据库加载策略
     * false: 从文件加载策略（仅用于测试）
     */
    var useDatabaseAdapter: Boolean = true,
    
    /**
     * 是否自动保存策略到数据库
     */
    var autoSave: Boolean = true,
    
    /**
     * 是否启用角色继承
     */
    var enableRoleHierarchy: Boolean = true,
    
    /**
     * 策略缓存配置
     */
    var cache: CacheConfig = CacheConfig()
)

/**
 * 缓存配置
 */
data class CacheConfig(
    /**
     * 是否启用缓存
     */
    var enabled: Boolean = true,
    
    /**
     * 缓存过期时间（秒）
     */
    var expireAfterWrite: Long = 3600,
    
    /**
     * 最大缓存条目数
     */
    var maximumSize: Long = 10000
)

