package com.vgerbot.wechat.wechat

import com.vgerbot.wechat.dao.WechatConfigDao
import com.vgerbot.wechat.entity.WechatConfig
import com.vgerbot.wechat.entity.WechatLoginType
import com.vgerbot.wechat.service.WechatServiceManager
import me.chanjar.weixin.mp.api.WxMpService
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl
import me.chanjar.weixin.open.api.WxOpenService
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl
import me.chanjar.weixin.open.api.impl.WxOpenInMemoryConfigStorage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 微信服务工厂
 * 
 * 管理微信公众号和开放平台服务实例
 */
@Component
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class WechatServiceFactory(
    private val wechatConfigDao: WechatConfigDao
) : WechatServiceManager {
    
    private val logger = LoggerFactory.getLogger(WechatServiceFactory::class.java)
    
    // 微信公众号服务缓存
    private val mpServiceCache = ConcurrentHashMap<String, WxMpService>()
    
    // 微信开放平台服务缓存
    private val openServiceCache = ConcurrentHashMap<String, WxOpenService>()
    
    @Volatile
    private var initialized = false
    
    /**
     * 获取微信公众号服务
     */
    fun getMpService(configId: String): WxMpService? {
        ensureInitialized()
        return mpServiceCache[configId] ?: loadMpService(configId)
    }
    
    /**
     * 获取微信开放平台服务
     */
    fun getOpenService(configId: String): WxOpenService? {
        ensureInitialized()
        return openServiceCache[configId] ?: loadOpenService(configId)
    }
    
    /**
     * 刷新所有服务
     */
    override fun refresh() {
        logger.info("Refreshing Wechat services...")
        
        mpServiceCache.clear()
        openServiceCache.clear()
        
        try {
            val configs = wechatConfigDao.findAllEnabled()
            
            configs.forEach { config ->
                try {
                    when (WechatLoginType.valueOf(config.loginType)) {
                        WechatLoginType.MP -> createMpService(config)?.let {
                            mpServiceCache[config.configId] = it
                        }
                        WechatLoginType.OPEN_PLATFORM -> createOpenService(config)?.let {
                            openServiceCache[config.configId] = it
                        }
                        WechatLoginType.MINI_PROGRAM -> {
                            // 小程序暂时使用公众号服务处理
                            createMpService(config)?.let {
                                mpServiceCache[config.configId] = it
                            }
                        }
                    }
                    logger.debug("Loaded Wechat config: ${config.configId}")
                } catch (e: Exception) {
                    logger.error("Failed to load Wechat config: ${config.configId}", e)
                }
            }
            
            initialized = true
            logger.info("Loaded ${mpServiceCache.size} MP services and ${openServiceCache.size} Open services")
        } catch (e: Exception) {
            logger.error("Failed to refresh Wechat services", e)
        }
    }
    
    /**
     * 确保已初始化
     */
    private fun ensureInitialized() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    refresh()
                }
            }
        }
    }
    
    /**
     * 加载微信公众号服务
     */
    private fun loadMpService(configId: String): WxMpService? {
        val config = wechatConfigDao.findByConfigId(configId) ?: return null
        
        if (config.loginType != WechatLoginType.MP.name && 
            config.loginType != WechatLoginType.MINI_PROGRAM.name) {
            return null
        }
        
        val service = createMpService(config) ?: return null
        mpServiceCache[configId] = service
        return service
    }
    
    /**
     * 加载微信开放平台服务
     */
    private fun loadOpenService(configId: String): WxOpenService? {
        val config = wechatConfigDao.findByConfigId(configId) ?: return null
        
        if (config.loginType != WechatLoginType.OPEN_PLATFORM.name) {
            return null
        }
        
        val service = createOpenService(config) ?: return null
        openServiceCache[configId] = service
        return service
    }
    
    /**
     * 创建微信公众号服务
     */
    private fun createMpService(config: WechatConfig): WxMpService? {
        return try {
            val wxMpConfig = WxMpDefaultConfigImpl().apply {
                appId = config.appId
                secret = config.appSecret
                token = config.token ?: ""
                aesKey = config.encodingAesKey ?: ""
            }
            
            WxMpServiceImpl().apply {
                setWxMpConfigStorage(wxMpConfig)
            }
        } catch (e: Exception) {
            logger.error("Failed to create WxMpService for ${config.configId}", e)
            null
        }
    }
    
    /**
     * 创建微信开放平台服务
     */
    private fun createOpenService(config: WechatConfig): WxOpenService? {
        return try {
            val wxOpenConfig = WxOpenInMemoryConfigStorage().apply {
                componentAppId = config.appId
                componentAppSecret = config.appSecret
                componentToken = config.token ?: ""
                componentAesKey = config.encodingAesKey ?: ""
            }
            
            WxOpenServiceImpl().apply {
                setWxOpenConfigStorage(wxOpenConfig)
            }
        } catch (e: Exception) {
            logger.error("Failed to create WxOpenService for ${config.configId}", e)
            null
        }
    }
    
    /**
     * 获取所有已加载的公众号服务配置 ID
     */
    fun getMpConfigIds(): Set<String> = mpServiceCache.keys.toSet()
    
    /**
     * 获取所有已加载的开放平台服务配置 ID
     */
    fun getOpenConfigIds(): Set<String> = openServiceCache.keys.toSet()
}

