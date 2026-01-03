package com.vgerbot.oauth.dao

import com.vgerbot.common.dao.AbstractSoftDeleteDao
import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.oauth.entity.OAuth2Provider
import com.vgerbot.oauth.entity.OAuth2Providers
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sortedBy
import org.ktorm.entity.toList
import org.ktorm.schema.ColumnDeclaring
import org.springframework.stereotype.Repository

/**
 * OAuth2 Provider DAO 接口
 */
interface OAuth2ProviderDao : SoftDeleteDao<OAuth2Provider, OAuth2Providers> {
    /**
     * 通过 registrationId 查找（未删除且启用的）
     */
    fun findByRegistrationId(registrationId: String): OAuth2Provider?
    
    /**
     * 查找所有启用的提供商（按排序顺序）
     */
    fun findAllEnabled(): List<OAuth2Provider>
}

/**
 * OAuth2 Provider DAO 实现
 */
@Repository
class OAuth2ProviderDaoImpl : AbstractSoftDeleteDao<OAuth2Provider, OAuth2Providers>(OAuth2Providers), OAuth2ProviderDao {
    
    override fun getIsDeletedColumn(table: OAuth2Providers): ColumnDeclaring<Boolean> = table.isDeleted
    
    override fun setDeleted(entity: OAuth2Provider, deleted: Boolean) {
        entity.isDeleted = deleted
    }
    
    override fun findByRegistrationId(registrationId: String): OAuth2Provider? {
        return database.sequenceOf(OAuth2Providers).find {
            (it.registrationId eq registrationId) and (it.isDeleted eq false) and (it.status eq 1)
        }
    }
    
    override fun findAllEnabled(): List<OAuth2Provider> {
        return database.sequenceOf(OAuth2Providers)
            .filter { (it.isDeleted eq false) and (it.status eq 1) }
            .sortedBy { it.sortOrder }
            .toList()
    }
}

