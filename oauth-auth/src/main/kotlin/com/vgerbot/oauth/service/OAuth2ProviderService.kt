package com.vgerbot.oauth.service

import com.vgerbot.oauth.dao.OAuth2ProviderDao
import com.vgerbot.oauth.dto.CreateOAuth2ProviderDto
import com.vgerbot.oauth.dto.OAuth2ProviderResponseDto
import com.vgerbot.oauth.dto.UpdateOAuth2ProviderDto
import com.vgerbot.oauth.entity.OAuth2Provider
import org.ktorm.dsl.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * OAuth2 Provider 服务接口
 */
interface OAuth2ProviderService {
    /**
     * 创建 OAuth2 提供商
     */
    fun create(dto: CreateOAuth2ProviderDto): OAuth2ProviderResponseDto?
    
    /**
     * 更新 OAuth2 提供商
     */
    fun update(id: Int, dto: UpdateOAuth2ProviderDto): OAuth2ProviderResponseDto?
    
    /**
     * 删除 OAuth2 提供商（逻辑删除）
     */
    fun delete(id: Int): Boolean
    
    /**
     * 获取单个 OAuth2 提供商
     */
    fun getById(id: Int): OAuth2ProviderResponseDto?
    
    /**
     * 获取所有启用的 OAuth2 提供商
     */
    fun getAllEnabled(): List<OAuth2ProviderResponseDto>
    
    /**
     * 获取所有 OAuth2 提供商（包括禁用的）
     */
    fun getAll(): List<OAuth2ProviderResponseDto>
    
    /**
     * 通过 registrationId 获取提供商（用于 ClientRegistration）
     */
    fun getByRegistrationId(registrationId: String): OAuth2Provider?
    
    /**
     * 刷新 OAuth2 客户端注册缓存
     */
    fun refreshClientRegistrations()
}

/**
 * OAuth2 Provider 服务实现
 */
@Service
class OAuth2ProviderServiceImpl(
    private val oauth2ProviderDao: OAuth2ProviderDao,
    private val clientRegistrationRefresher: ClientRegistrationRefresher?
) : OAuth2ProviderService {
    
    private val logger = LoggerFactory.getLogger(OAuth2ProviderServiceImpl::class.java)
    
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
    
    @Transactional
    override fun create(dto: CreateOAuth2ProviderDto): OAuth2ProviderResponseDto? {
        // 检查 registrationId 是否已存在
        val existing = oauth2ProviderDao.findOneActive { it.registrationId eq dto.registrationId }
        if (existing != null) {
            logger.warn("OAuth2 provider with registrationId ${dto.registrationId} already exists")
            return null
        }
        
        val entity = OAuth2Provider {
            registrationId = dto.registrationId
            name = dto.name
            clientId = dto.clientId
            clientSecret = dto.clientSecret
            authorizationUri = dto.authorizationUri
            tokenUri = dto.tokenUri
            userInfoUri = dto.userInfoUri
            jwkSetUri = dto.jwkSetUri
            issuerUri = dto.issuerUri
            redirectUri = dto.redirectUri
            scopes = dto.scopes
            userNameAttributeName = dto.userNameAttributeName
            status = dto.status
            sortOrder = dto.sortOrder
            description = dto.description
            createdAt = Instant.now()
            updatedAt = null
            isDeleted = false
        }
        
        val result = oauth2ProviderDao.add(entity)
        if (result != 1) {
            logger.error("Failed to create OAuth2 provider: ${dto.registrationId}")
            return null
        }
        
        logger.info("Created OAuth2 provider: ${dto.registrationId}")
        
        // 刷新客户端注册缓存
        refreshClientRegistrations()
        
        return toResponseDto(entity)
    }
    
    @Transactional
    override fun update(id: Int, dto: UpdateOAuth2ProviderDto): OAuth2ProviderResponseDto? {
        val entity = oauth2ProviderDao.findOneActive { it.id eq id }
            ?: return null
        
        dto.name?.let { entity.name = it }
        dto.clientId?.let { entity.clientId = it }
        dto.clientSecret?.let { entity.clientSecret = it }
        dto.authorizationUri?.let { entity.authorizationUri = it }
        dto.tokenUri?.let { entity.tokenUri = it }
        dto.userInfoUri?.let { entity.userInfoUri = it }
        dto.jwkSetUri?.let { entity.jwkSetUri = it }
        dto.issuerUri?.let { entity.issuerUri = it }
        dto.redirectUri?.let { entity.redirectUri = it }
        dto.scopes?.let { entity.scopes = it }
        dto.userNameAttributeName?.let { entity.userNameAttributeName = it }
        dto.status?.let { entity.status = it }
        dto.sortOrder?.let { entity.sortOrder = it }
        dto.description?.let { entity.description = it }
        entity.updatedAt = Instant.now()
        
        val result = oauth2ProviderDao.update(entity)
        if (result != 1) {
            logger.error("Failed to update OAuth2 provider: $id")
            return null
        }
        
        logger.info("Updated OAuth2 provider: ${entity.registrationId}")
        
        // 刷新客户端注册缓存
        refreshClientRegistrations()
        
        return toResponseDto(entity)
    }
    
    @Transactional
    override fun delete(id: Int): Boolean {
        val entity = oauth2ProviderDao.findOneActive { it.id eq id }
            ?: return false
        
        val registrationId = entity.registrationId
        val result = oauth2ProviderDao.softDelete(id)
        
        if (result) {
            logger.info("Deleted OAuth2 provider: $registrationId")
            // 刷新客户端注册缓存
            refreshClientRegistrations()
        }
        
        return result
    }
    
    override fun getById(id: Int): OAuth2ProviderResponseDto? {
        val entity = oauth2ProviderDao.findOneActive { it.id eq id }
            ?: return null
        return toResponseDto(entity)
    }
    
    override fun getAllEnabled(): List<OAuth2ProviderResponseDto> {
        return oauth2ProviderDao.findAllEnabled().map { toResponseDto(it) }
    }
    
    override fun getAll(): List<OAuth2ProviderResponseDto> {
        return oauth2ProviderDao.findAllActive { _ -> true }.map { toResponseDto(it) }
    }
    
    override fun getByRegistrationId(registrationId: String): OAuth2Provider? {
        return oauth2ProviderDao.findByRegistrationId(registrationId)
    }
    
    override fun refreshClientRegistrations() {
        clientRegistrationRefresher?.refresh()
    }
    
    private fun toResponseDto(entity: OAuth2Provider): OAuth2ProviderResponseDto {
        return OAuth2ProviderResponseDto(
            id = entity.id,
            registrationId = entity.registrationId,
            name = entity.name,
            clientId = entity.clientId,
            authorizationUri = entity.authorizationUri,
            tokenUri = entity.tokenUri,
            userInfoUri = entity.userInfoUri,
            jwkSetUri = entity.jwkSetUri,
            issuerUri = entity.issuerUri,
            redirectUri = entity.redirectUri,
            scopes = entity.scopes.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            userNameAttributeName = entity.userNameAttributeName,
            status = entity.status,
            sortOrder = entity.sortOrder,
            description = entity.description,
            createdAt = DATE_FORMATTER.format(entity.createdAt),
            updatedAt = entity.updatedAt?.let { DATE_FORMATTER.format(it) }
        )
    }
}

/**
 * 客户端注册刷新器接口
 * 用于在 OAuth2 提供商配置变更时刷新 ClientRegistrationRepository
 */
interface ClientRegistrationRefresher {
    fun refresh()
}

