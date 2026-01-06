package com.vgerbot.oauth2server.service

import com.vgerbot.oauth2server.dao.OAuth2ClientDao
import com.vgerbot.oauth2server.data.OAuth2ClientCreateRequest
import com.vgerbot.oauth2server.data.OAuth2ClientResponse
import com.vgerbot.oauth2server.data.OAuth2ClientUpdateRequest
import com.vgerbot.oauth2server.entity.OAuth2Client
import com.vgerbot.oauth2server.entity.OAuth2Clients
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.entity.update
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * OAuth2 客户端服务
 */
@Service
class OAuth2ClientService(
    private val oauth2ClientDao: OAuth2ClientDao,
    private val database: Database,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(OAuth2ClientService::class.java)

    /**
     * 创建 OAuth2 客户端
     */
    @Transactional
    fun createClient(request: OAuth2ClientCreateRequest, createdBy: Int? = null): OAuth2ClientResponse {
        // 检查客户端 ID 是否已存在
        val existing = oauth2ClientDao.findByClientId(request.clientId)
        if (existing != null) {
            throw IllegalArgumentException("Client ID already exists: ${request.clientId}")
        }

        // 创建客户端实体
        val client = OAuth2Client {
            this.clientId = request.clientId
            this.clientSecret = passwordEncoder.encode(request.clientSecret)
            this.clientName = request.clientName
            this.description = request.description
            this.clientAuthenticationMethods = request.clientAuthenticationMethods.joinToString(",")
            this.authorizationGrantTypes = request.authorizationGrantTypes.joinToString(",")
            this.redirectUris = request.redirectUris?.joinToString(",")
            this.scopes = request.scopes?.joinToString(",")
            this.requireProofKey = request.requireProofKey
            this.accessTokenValiditySeconds = request.accessTokenValiditySeconds
            this.refreshTokenValiditySeconds = request.refreshTokenValiditySeconds
            this.enabled = request.enabled
            this.createdAt = Instant.now()
            this.createdBy = createdBy
            this.isDeleted = false
        }

        database.sequenceOf(OAuth2Clients).add(client)
        logger.info("Created OAuth2 client: ${request.clientId}")

        return toResponse(client)
    }

    /**
     * 更新 OAuth2 客户端
     */
    @Transactional
    fun updateClient(clientId: String, request: OAuth2ClientUpdateRequest, updatedBy: Int? = null): OAuth2ClientResponse {
        val client = oauth2ClientDao.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")

        request.clientName?.let { client.clientName = it }
        request.description?.let { client.description = it }
        request.clientAuthenticationMethods?.let { 
            client.clientAuthenticationMethods = it.joinToString(",") 
        }
        request.authorizationGrantTypes?.let { 
            client.authorizationGrantTypes = it.joinToString(",") 
        }
        request.redirectUris?.let { client.redirectUris = it.joinToString(",") }
        request.scopes?.let { client.scopes = it.joinToString(",") }
        request.requireProofKey?.let { client.requireProofKey = it }
        request.accessTokenValiditySeconds?.let { client.accessTokenValiditySeconds = it }
        request.refreshTokenValiditySeconds?.let { client.refreshTokenValiditySeconds = it }
        request.enabled?.let { client.enabled = it }

        client.updatedAt = Instant.now()
        client.updatedBy = updatedBy

        database.sequenceOf(OAuth2Clients).update(client)
        logger.info("Updated OAuth2 client: $clientId")

        return toResponse(client)
    }

    /**
     * 删除 OAuth2 客户端（逻辑删除）
     */
    @Transactional
    fun deleteClient(clientId: String, deletedBy: Int? = null) {
        val client = oauth2ClientDao.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")

        oauth2ClientDao.softDelete(client.id)
        logger.info("Deleted OAuth2 client: $clientId")
    }

    /**
     * 根据客户端 ID 查找客户端
     */
    fun findByClientId(clientId: String): OAuth2ClientResponse? {
        val client = oauth2ClientDao.findByClientId(clientId) ?: return null
        return toResponse(client)
    }

    /**
     * 获取所有启用的客户端
     */
    fun findAllEnabled(): List<OAuth2ClientResponse> {
        return database.sequenceOf(OAuth2Clients)
            .filter { (it.isDeleted eq false) and (it.enabled eq true) }
            .map { toResponse(it) }
    }

    /**
     * 获取所有客户端
     */
    fun findAll(): List<OAuth2ClientResponse> {
        return oauth2ClientDao.findAllActive().map { toResponse(it) }
    }

    /**
     * 转换为响应对象
     */
    private fun toResponse(client: OAuth2Client): OAuth2ClientResponse {
        return OAuth2ClientResponse(
            id = client.id,
            clientId = client.clientId,
            clientName = client.clientName,
            description = client.description,
            clientAuthenticationMethods = client.clientAuthenticationMethods.split(",").filter { it.isNotBlank() },
            authorizationGrantTypes = client.authorizationGrantTypes.split(",").filter { it.isNotBlank() },
            redirectUris = client.redirectUris?.split(",")?.filter { it.isNotBlank() },
            scopes = client.scopes?.split(",")?.filter { it.isNotBlank() },
            requireProofKey = client.requireProofKey,
            accessTokenValiditySeconds = client.accessTokenValiditySeconds,
            refreshTokenValiditySeconds = client.refreshTokenValiditySeconds,
            enabled = client.enabled,
            createdAt = client.createdAt,
            updatedAt = client.updatedAt
        )
    }
}

