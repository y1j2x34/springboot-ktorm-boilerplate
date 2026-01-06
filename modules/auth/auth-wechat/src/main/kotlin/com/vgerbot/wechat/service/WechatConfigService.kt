package com.vgerbot.wechat.service

import com.vgerbot.wechat.dao.WechatConfigDao
import com.vgerbot.wechat.dto.CreateWechatConfigDto
import com.vgerbot.wechat.dto.UpdateWechatConfigDto
import com.vgerbot.wechat.dto.WechatConfigResponseDto
import com.vgerbot.wechat.entity.WechatConfig
import com.vgerbot.wechat.entity.WechatLoginType
import org.ktorm.dsl.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * 微信配置服务接口
 */
interface WechatConfigService {
    fun create(dto: CreateWechatConfigDto): WechatConfigResponseDto?
    fun update(id: Int, dto: UpdateWechatConfigDto): WechatConfigResponseDto?
    fun delete(id: Int): Boolean
    fun getById(id: Int): WechatConfigResponseDto?
    fun getByConfigId(configId: String): WechatConfig?
    fun getAllEnabled(): List<WechatConfigResponseDto>
    fun getAll(): List<WechatConfigResponseDto>
    fun getByLoginType(loginType: WechatLoginType): List<WechatConfigResponseDto>
    fun refreshWechatServices()
}

/**
 * 微信配置服务实现
 */
@Service
class WechatConfigServiceImpl(
    private val wechatConfigDao: WechatConfigDao,
    private val wechatServiceManager: WechatServiceManager?
) : WechatConfigService {
    
    private val logger = LoggerFactory.getLogger(WechatConfigServiceImpl::class.java)
    
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
    
    @Transactional
    override fun create(dto: CreateWechatConfigDto): WechatConfigResponseDto? {
        // 验证登录类型
        try {
            WechatLoginType.valueOf(dto.loginType)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid login type: ${dto.loginType}")
            return null
        }
        
        // 检查 configId 是否已存在
        val existing = wechatConfigDao.findOneActive { it.configId eq dto.configId }
        if (existing != null) {
            logger.warn("Wechat config with configId ${dto.configId} already exists")
            return null
        }
        
        val entity = WechatConfig {
            configId = dto.configId
            name = dto.name
            loginType = dto.loginType
            appId = dto.appId
            appSecret = dto.appSecret
            token = dto.token
            encodingAesKey = dto.encodingAesKey
            status = dto.status
            sortOrder = dto.sortOrder
            description = dto.description
            tenantId = dto.tenantId
            createdAt = Instant.now()
            updatedAt = null
            isDeleted = false
        }
        
        val result = wechatConfigDao.add(entity)
        if (result != 1) {
            logger.error("Failed to create Wechat config: ${dto.configId}")
            return null
        }
        
        logger.info("Created Wechat config: ${dto.configId}")
        
        // 刷新微信服务
        refreshWechatServices()
        
        return toResponseDto(entity)
    }
    
    @Transactional
    override fun update(id: Int, dto: UpdateWechatConfigDto): WechatConfigResponseDto? {
        val entity = wechatConfigDao.findOneActive { it.id eq id }
            ?: return null
        
        dto.name?.let { entity.name = it }
        dto.appId?.let { entity.appId = it }
        dto.appSecret?.let { entity.appSecret = it }
        dto.token?.let { entity.token = it }
        dto.encodingAesKey?.let { entity.encodingAesKey = it }
        dto.status?.let { entity.status = it }
        dto.sortOrder?.let { entity.sortOrder = it }
        dto.description?.let { entity.description = it }
        dto.tenantId?.let { entity.tenantId = it }
        entity.updatedAt = Instant.now()
        
        val result = wechatConfigDao.update(entity)
        if (result != 1) {
            logger.error("Failed to update Wechat config: $id")
            return null
        }
        
        logger.info("Updated Wechat config: ${entity.configId}")
        
        // 刷新微信服务
        refreshWechatServices()
        
        return toResponseDto(entity)
    }
    
    @Transactional
    override fun delete(id: Int): Boolean {
        val entity = wechatConfigDao.findOneActive { it.id eq id }
            ?: return false
        
        val configId = entity.configId
        val result = wechatConfigDao.softDelete(id)
        
        if (result) {
            logger.info("Deleted Wechat config: $configId")
            refreshWechatServices()
        }
        
        return result
    }
    
    override fun getById(id: Int): WechatConfigResponseDto? {
        val entity = wechatConfigDao.findOneActive { it.id eq id }
            ?: return null
        return toResponseDto(entity)
    }
    
    override fun getByConfigId(configId: String): WechatConfig? {
        return wechatConfigDao.findByConfigId(configId)
    }
    
    override fun getAllEnabled(): List<WechatConfigResponseDto> {
        return wechatConfigDao.findAllEnabled().map { toResponseDto(it) }
    }
    
    override fun getAll(): List<WechatConfigResponseDto> {
        return wechatConfigDao.findAllActive().map { toResponseDto(it) }
    }
    
    override fun getByLoginType(loginType: WechatLoginType): List<WechatConfigResponseDto> {
        return wechatConfigDao.findByLoginType(loginType.name).map { toResponseDto(it) }
    }
    
    override fun refreshWechatServices() {
        wechatServiceManager?.refresh()
    }
    
    private fun toResponseDto(entity: WechatConfig): WechatConfigResponseDto {
        val loginUrl = when (WechatLoginType.valueOf(entity.loginType)) {
            WechatLoginType.OPEN_PLATFORM -> "/public/wechat/open/${entity.configId}/login"
            WechatLoginType.MP -> "/public/wechat/mp/${entity.configId}/login"
            WechatLoginType.MINI_PROGRAM -> "/public/wechat/mini/${entity.configId}/login"
        }
        
        return WechatConfigResponseDto(
            id = entity.id,
            configId = entity.configId,
            name = entity.name,
            loginType = entity.loginType,
            appId = entity.appId,
            status = entity.status,
            sortOrder = entity.sortOrder,
            description = entity.description,
            tenantId = entity.tenantId,
            loginUrl = loginUrl,
            createdAt = DATE_FORMATTER.format(entity.createdAt),
            updatedAt = entity.updatedAt?.let { DATE_FORMATTER.format(it) }
        )
    }
}

/**
 * 微信服务管理器接口
 * 用于刷新微信服务配置
 */
interface WechatServiceManager {
    fun refresh()
}

