package com.vgerbot.app.configuration

import com.vgerbot.authorization.entity.Permissions
import com.vgerbot.authorization.entity.RolePermissions
import com.vgerbot.authorization.entity.Roles
import com.vgerbot.authorization.entity.UserPermissions
import com.vgerbot.authorization.entity.UserRoles
import com.vgerbot.dict.entity.DictDatas
import com.vgerbot.dict.entity.DictTypes
import com.vgerbot.oauth.entity.OAuth2Providers
import com.vgerbot.postgrest.api.TableRegistry
import com.vgerbot.tenant.entity.Tenants
import com.vgerbot.tenant.entity.UserTenants
import com.vgerbot.user.entity.Users
import com.vgerbot.wechat.entity.WechatConfigs
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

/**
 * PostgREST 表注册配置
 * 
 * 注册所有可通过 PostgREST API 查询的表
 * 只有注册过的表才能被查询，这提供了一层安全保护
 */
@Configuration
class PostgrestTableConfiguration(
    private val tableRegistry: TableRegistry
) {
    
    private val logger = LoggerFactory.getLogger(PostgrestTableConfiguration::class.java)
    
    @PostConstruct
    fun registerTables() {
        logger.info("Registering tables for PostgREST query...")
        
        // ==================== User 模块 ====================
        tableRegistry.registerTable("users", Users)
        tableRegistry.registerTable("user", Users)  // 别名
        
        // ==================== Tenant 模块 ====================
        tableRegistry.registerTable("tenants", Tenants)
        tableRegistry.registerTable("tenant", Tenants)  // 别名
        tableRegistry.registerTable("user_tenants", UserTenants)
        tableRegistry.registerTable("user_tenant", UserTenants)  // 别名
        
        // ==================== Authorization 模块 ====================
        tableRegistry.registerTable("roles", Roles)
        tableRegistry.registerTable("role", Roles)  // 别名
        tableRegistry.registerTable("permissions", Permissions)
        tableRegistry.registerTable("permission", Permissions)  // 别名
        tableRegistry.registerTable("user_roles", UserRoles)
        tableRegistry.registerTable("user_role", UserRoles)  // 别名
        tableRegistry.registerTable("role_permissions", RolePermissions)
        tableRegistry.registerTable("role_permission", RolePermissions)  // 别名
        tableRegistry.registerTable("user_permissions", UserPermissions)
        tableRegistry.registerTable("user_permission", UserPermissions)  // 别名
        
        // ==================== Dict 模块 ====================
        tableRegistry.registerTable("dict_types", DictTypes)
        tableRegistry.registerTable("dict_type", DictTypes)  // 别名
        tableRegistry.registerTable("dict_datas", DictDatas)
        tableRegistry.registerTable("dict_data", DictDatas)  // 别名
        
        // ==================== OAuth 模块 ====================
        tableRegistry.registerTable("oauth2_providers", OAuth2Providers)
        tableRegistry.registerTable("oauth2_provider", OAuth2Providers)  // 别名
        
        // ==================== WeChat 模块 ====================
        tableRegistry.registerTable("wechat_configs", WechatConfigs)
        tableRegistry.registerTable("wechat_config", WechatConfigs)  // 别名
        
        logger.info("Registered {} tables for PostgREST query: {}", 
            tableRegistry.getRegisteredTableNames().size,
            tableRegistry.getRegisteredTableNames().joinToString(", "))
    }
}

