package com.vgerbot.oauth2server.entity

import com.vgerbot.common.entity.AuditableEntity
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

/**
 * OAuth2 客户端实体
 * 
 * 用于存储 OAuth2 客户端注册信息
 */
interface OAuth2Client : AuditableEntity<OAuth2Client> {
    companion object : Entity.Factory<OAuth2Client>()

    val id: Int

    /**
     * 客户端 ID（唯一标识符）
     */
    var clientId: String

    /**
     * 客户端密钥（加密存储）
     */
    var clientSecret: String

    /**
     * 客户端名称
     */
    var clientName: String

    /**
     * 客户端描述
     */
    var description: String?

    /**
     * 客户端认证方法（client_secret_basic, client_secret_post, private_key_jwt, none）
     */
    var clientAuthenticationMethods: String

    /**
     * 授权类型（authorization_code, refresh_token, client_credentials, etc.）
     * 多个值用逗号分隔
     */
    var authorizationGrantTypes: String

    /**
     * 重定向 URI
     * 多个值用逗号分隔
     */
    var redirectUris: String?

    /**
     * 作用域（scope）
     * 多个值用逗号分隔
     */
    var scopes: String?

    /**
     * 是否需要用户同意（PKCE）
     */
    var requireProofKey: Boolean

    /**
     * Access Token 有效期（秒）
     */
    var accessTokenValiditySeconds: Int?

    /**
     * Refresh Token 有效期（秒）
     */
    var refreshTokenValiditySeconds: Int?

    /**
     * 是否启用
     */
    var enabled: Boolean
}

object OAuth2Clients : com.vgerbot.common.entity.AuditableTable<OAuth2Client>("oauth2_client") {
    val id = int("id").primaryKey().bindTo { it.id }
    val clientId = varchar("client_id").unique().bindTo { it.clientId }
    val clientSecret = varchar("client_secret").bindTo { it.clientSecret }
    val clientName = varchar("client_name").bindTo { it.clientName }
    val description = text("description").bindTo { it.description }
    val clientAuthenticationMethods = varchar("client_authentication_methods").bindTo { it.clientAuthenticationMethods }
    val authorizationGrantTypes = varchar("authorization_grant_types").bindTo { it.authorizationGrantTypes }
    val redirectUris = text("redirect_uris").bindTo { it.redirectUris }
    val scopes = text("scopes").bindTo { it.scopes }
    val requireProofKey = boolean("require_proof_key").bindTo { it.requireProofKey }
    val accessTokenValiditySeconds = int("access_token_validity_seconds").bindTo { it.accessTokenValiditySeconds }
    val refreshTokenValiditySeconds = int("refresh_token_validity_seconds").bindTo { it.refreshTokenValiditySeconds }
    val enabled = boolean("enabled").bindTo { it.enabled }
}

val Database.oauth2Clients get() = this.sequenceOf(OAuth2Clients)

