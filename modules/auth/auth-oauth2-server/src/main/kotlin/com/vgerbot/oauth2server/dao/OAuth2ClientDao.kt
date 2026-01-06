package com.vgerbot.oauth2server.dao

import com.vgerbot.common.dao.SoftDeleteDao
import com.vgerbot.oauth2server.entity.OAuth2Client
import com.vgerbot.oauth2server.entity.OAuth2Clients

/**
 * OAuth2 客户端 DAO 接口
 */
interface OAuth2ClientDao : SoftDeleteDao<OAuth2Client, OAuth2Clients> {
    /**
     * 根据客户端 ID 查找客户端
     */
    fun findByClientId(clientId: String): OAuth2Client?
}

