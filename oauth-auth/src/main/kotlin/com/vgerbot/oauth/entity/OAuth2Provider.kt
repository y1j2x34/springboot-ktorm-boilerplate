package com.vgerbot.oauth.entity

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

/**
 * OAuth2 Provider 实体
 */
interface OAuth2Provider : Entity<OAuth2Provider> {
    companion object : Entity.Factory<OAuth2Provider>()
    
    val id: Int
    
    /** 注册 ID（唯一标识，如 google、github） */
    var registrationId: String
    
    /** 提供商名称（显示用） */
    var name: String
    
    /** 客户端 ID */
    var clientId: String
    
    /** 客户端密钥 */
    var clientSecret: String
    
    /** 授权端点 URI */
    var authorizationUri: String?
    
    /** Token 端点 URI */
    var tokenUri: String?
    
    /** 用户信息端点 URI */
    var userInfoUri: String?
    
    /** JWK Set URI（用于验证 ID Token） */
    var jwkSetUri: String?
    
    /** Issuer URI（OIDC 自动发现） */
    var issuerUri: String?
    
    /** 重定向 URI */
    var redirectUri: String?
    
    /** 请求的权限范围（逗号分隔） */
    var scopes: String
    
    /** 用户名属性名 */
    var userNameAttributeName: String
    
    /** 状态：1-启用，0-禁用 */
    var status: Int
    
    /** 排序顺序 */
    var sortOrder: Int
    
    /** 描述 */
    var description: String?
    
    /** 创建时间 */
    var createdAt: Instant
    
    /** 更新时间 */
    var updatedAt: Instant?
    
    /** 是否删除 */
    var isDeleted: Boolean
}

/**
 * OAuth2 Provider 表定义
 */
object OAuth2Providers : Table<OAuth2Provider>("oauth2_provider") {
    val id = int("id").primaryKey().bindTo { it.id }
    val registrationId = varchar("registration_id").bindTo { it.registrationId }
    val name = varchar("name").bindTo { it.name }
    val clientId = varchar("client_id").bindTo { it.clientId }
    val clientSecret = varchar("client_secret").bindTo { it.clientSecret }
    val authorizationUri = varchar("authorization_uri").bindTo { it.authorizationUri }
    val tokenUri = varchar("token_uri").bindTo { it.tokenUri }
    val userInfoUri = varchar("user_info_uri").bindTo { it.userInfoUri }
    val jwkSetUri = varchar("jwk_set_uri").bindTo { it.jwkSetUri }
    val issuerUri = varchar("issuer_uri").bindTo { it.issuerUri }
    val redirectUri = varchar("redirect_uri").bindTo { it.redirectUri }
    val scopes = varchar("scopes").bindTo { it.scopes }
    val userNameAttributeName = varchar("user_name_attribute_name").bindTo { it.userNameAttributeName }
    val status = int("status").bindTo { it.status }
    val sortOrder = int("sort_order").bindTo { it.sortOrder }
    val description = varchar("description").bindTo { it.description }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}

val Database.oauth2Providers get() = this.sequenceOf(OAuth2Providers)

