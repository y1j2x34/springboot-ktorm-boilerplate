package com.vgerbot.wechat.dao

import com.vgerbot.common.dao.AbstractSoftDeleteDao
import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.wechat.entity.WechatConfig
import com.vgerbot.wechat.entity.WechatConfigs
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.sortedBy
import org.ktorm.entity.toList
import org.ktorm.schema.ColumnDeclaring
import org.springframework.stereotype.Repository

/**
 * 微信配置 DAO 接口
 */
interface WechatConfigDao : SoftDeleteDao<WechatConfig, WechatConfigs> {
    /**
     * 通过 configId 查找（未删除且启用的）
     */
    fun findByConfigId(configId: String): WechatConfig?
    
    /**
     * 通过 appId 查找（未删除且启用的）
     */
    fun findByAppId(appId: String): WechatConfig?
    
    /**
     * 通过登录类型查找所有启用的配置
     */
    fun findByLoginType(loginType: String): List<WechatConfig>
    
    /**
     * 查找所有启用的配置（按排序顺序）
     */
    fun findAllEnabled(): List<WechatConfig>
}

/**
 * 微信配置 DAO 实现
 */
@Repository
class WechatConfigDaoImpl : AbstractSoftDeleteDao<WechatConfig, WechatConfigs>(WechatConfigs), WechatConfigDao {
    
    override fun getIsDeletedColumn(table: WechatConfigs): ColumnDeclaring<Boolean> = table.isDeleted
    
    override fun setDeleted(entity: WechatConfig, deleted: Boolean) {
        entity.isDeleted = deleted
    }
    
    override fun findByConfigId(configId: String): WechatConfig? {
        return database.sequenceOf(WechatConfigs).find {
            (it.configId eq configId) and (it.isDeleted eq false) and (it.status eq 1)
        }
    }
    
    override fun findByAppId(appId: String): WechatConfig? {
        return database.sequenceOf(WechatConfigs).find {
            (it.appId eq appId) and (it.isDeleted eq false) and (it.status eq 1)
        }
    }
    
    override fun findByLoginType(loginType: String): List<WechatConfig> {
        return database.sequenceOf(WechatConfigs)
            .filter { (it.loginType eq loginType) and (it.isDeleted eq false) and (it.status eq 1) }
            .sortedBy { it.sortOrder }
            .toList()
    }
    
    override fun findAllEnabled(): List<WechatConfig> {
        return database.sequenceOf(WechatConfigs)
            .filter { (it.isDeleted eq false) and (it.status eq 1) }
            .sortedBy { it.sortOrder }
            .toList()
    }
}

