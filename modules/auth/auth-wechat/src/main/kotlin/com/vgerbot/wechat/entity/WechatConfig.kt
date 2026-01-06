package com.vgerbot.wechat.entity

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
 * 微信登录类型
 */
enum class WechatLoginType {
    /** 微信开放平台 - 网站应用（PC 扫码登录） */
    OPEN_PLATFORM,
    
    /** 微信公众号 - 网页授权（微信内 H5） */
    MP,
    
    /** 微信小程序 */
    MINI_PROGRAM
}

/**
 * 微信配置实体
 */
interface WechatConfig : Entity<WechatConfig> {
    companion object : Entity.Factory<WechatConfig>()
    
    val id: Int
    
    /** 配置标识（唯一） */
    var configId: String
    
    /** 配置名称（显示用） */
    var name: String
    
    /** 登录类型 */
    var loginType: String
    
    /** AppID */
    var appId: String
    
    /** AppSecret */
    var appSecret: String
    
    /** Token（公众号验证用） */
    var token: String?
    
    /** EncodingAESKey（消息加解密密钥） */
    var encodingAesKey: String?
    
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
 * 微信配置表定义
 */
object WechatConfigs : Table<WechatConfig>("wechat_config") {
    val id = int("id").primaryKey().bindTo { it.id }
    val configId = varchar("config_id").bindTo { it.configId }
    val name = varchar("name").bindTo { it.name }
    val loginType = varchar("login_type").bindTo { it.loginType }
    val appId = varchar("app_id").bindTo { it.appId }
    val appSecret = varchar("app_secret").bindTo { it.appSecret }
    val token = varchar("token").bindTo { it.token }
    val encodingAesKey = varchar("encoding_aes_key").bindTo { it.encodingAesKey }
    val status = int("status").bindTo { it.status }
    val sortOrder = int("sort_order").bindTo { it.sortOrder }
    val description = varchar("description").bindTo { it.description }
    val createdAt = timestamp("created_at").bindTo { it.createdAt }
    val updatedAt = timestamp("updated_at").bindTo { it.updatedAt }
    val isDeleted = boolean("is_deleted").bindTo { it.isDeleted }
}

val Database.wechatConfigs get() = this.sequenceOf(WechatConfigs)

