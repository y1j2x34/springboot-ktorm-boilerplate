package com.vgerbot.oauth2server.dao

import com.vgerbot.common.dao.AuditableDaoImpl
import com.vgerbot.oauth2server.entity.OAuth2Client
import com.vgerbot.oauth2server.entity.OAuth2Clients
import org.ktorm.dsl.eq
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * OAuth2 客户端 DAO 实现
 */
@Repository
class OAuth2ClientDaoImpl : AuditableDaoImpl<OAuth2Client, OAuth2Clients>(OAuth2Clients), OAuth2ClientDao {

    override fun findByClientId(clientId: String): OAuth2Client? {
        return findOneActive { it.clientId eq clientId }
    }

    override fun softDelete(id: Any): Boolean {
        val clientId = id as? Int ?: return false
        val entity = findOneActive { it.id eq clientId } ?: return false
        entity.isDeleted = true
        entity.updatedAt = Instant.now()
        return update(entity) == 1
    }
}

