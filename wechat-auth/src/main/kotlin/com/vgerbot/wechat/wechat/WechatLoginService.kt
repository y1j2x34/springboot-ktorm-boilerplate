package com.vgerbot.wechat.wechat

import com.vgerbot.auth.CustomUserDetails
import com.vgerbot.auth.ExtendedUserDetails
import com.vgerbot.auth.JwtProperties
import com.vgerbot.auth.JwtTokenUtils
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.wechat.dao.WechatConfigDao
import com.vgerbot.wechat.dto.WechatUserInfo
import com.vgerbot.wechat.entity.WechatLoginType
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.service.UserService
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * 微信登录服务
 * 
 * 处理微信 OAuth2 登录流程
 */
@Service
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class WechatLoginService(
    private val wechatServiceFactory: WechatServiceFactory,
    private val wechatConfigDao: WechatConfigDao,
    private val userService: UserService,
    private val jwtTokenUtils: JwtTokenUtils,
    private val jwtProperties: JwtProperties
) {
    
    private val logger = LoggerFactory.getLogger(WechatLoginService::class.java)
    private val restTemplate = RestTemplate()
    
    companion object {
        // 微信开放平台 OAuth2 授权 URL
        private const val OPEN_PLATFORM_AUTH_URL = "https://open.weixin.qq.com/connect/qrconnect"
        
        // 微信公众号 OAuth2 授权 URL
        private const val MP_AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize"
        
        // 微信 SNS API（网站应用扫码登录用）
        private const val SNS_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token"
        private const val SNS_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo"
        
        // 微信小程序 API
        private const val MINI_JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session"
    }
    
    /**
     * 生成微信开放平台授权 URL（PC 扫码登录）
     */
    fun generateOpenPlatformAuthUrl(configId: String, redirectUri: String, state: String?): String? {
        val config = wechatConfigDao.findByConfigId(configId) ?: return null
        
        if (config.loginType != WechatLoginType.OPEN_PLATFORM.name) {
            logger.warn("Config $configId is not OPEN_PLATFORM type")
            return null
        }
        
        val actualState = state ?: UUID.randomUUID().toString()
        val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        
        return "$OPEN_PLATFORM_AUTH_URL?" +
                "appid=${config.appId}" +
                "&redirect_uri=$encodedRedirectUri" +
                "&response_type=code" +
                "&scope=snsapi_login" +
                "&state=$actualState" +
                "#wechat_redirect"
    }
    
    /**
     * 生成微信公众号授权 URL（微信内 H5）
     */
    fun generateMpAuthUrl(configId: String, redirectUri: String, state: String?, scope: String = "snsapi_userinfo"): String? {
        val config = wechatConfigDao.findByConfigId(configId) ?: return null
        
        if (config.loginType != WechatLoginType.MP.name) {
            logger.warn("Config $configId is not MP type")
            return null
        }
        
        val actualState = state ?: UUID.randomUUID().toString()
        val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        
        return "$MP_AUTH_URL?" +
                "appid=${config.appId}" +
                "&redirect_uri=$encodedRedirectUri" +
                "&response_type=code" +
                "&scope=$scope" +
                "&state=$actualState" +
                "#wechat_redirect"
    }
    
    /**
     * 处理微信开放平台回调（网站应用 PC 扫码登录）
     * 
     * 网站应用使用 SNS API，而非第三方平台的 Component API
     */
    fun handleOpenPlatformCallback(configId: String, code: String): TokenResponse? {
        val config = wechatConfigDao.findByConfigId(configId)
        if (config == null) {
            logger.warn("Wechat config not found: $configId")
            return null
        }
        
        return try {
            // 网站应用扫码登录使用 SNS API
            // 1. 使用 code 换取 access_token
            val tokenUrl = "$SNS_ACCESS_TOKEN_URL?" +
                    "appid=${config.appId}" +
                    "&secret=${config.appSecret}" +
                    "&code=$code" +
                    "&grant_type=authorization_code"
            
            val tokenResponse = restTemplate.getForObject(tokenUrl, Map::class.java)
            
            if (tokenResponse == null || tokenResponse.containsKey("errcode")) {
                val errcode = tokenResponse?.get("errcode")
                val errmsg = tokenResponse?.get("errmsg")
                logger.error("Failed to get access_token: errcode=$errcode, errmsg=$errmsg")
                return null
            }
            
            val accessToken = tokenResponse["access_token"] as? String
            val openid = tokenResponse["openid"] as? String
            val unionid = tokenResponse["unionid"] as? String
            
            if (accessToken == null || openid == null) {
                logger.error("Invalid token response: missing access_token or openid")
                return null
            }
            
            // 2. 使用 access_token 获取用户信息
            val userInfoUrl = "$SNS_USERINFO_URL?" +
                    "access_token=$accessToken" +
                    "&openid=$openid" +
                    "&lang=zh_CN"
            
            val userInfoResponse = restTemplate.getForObject(userInfoUrl, Map::class.java)
            
            if (userInfoResponse == null || userInfoResponse.containsKey("errcode")) {
                // 如果获取用户信息失败，至少可以用 openid 登录
                val wechatUserInfo = WechatUserInfo(
                    openid = openid,
                    unionid = unionid,
                    nickname = null,
                    headimgurl = null,
                    sex = null,
                    province = null,
                    city = null,
                    country = null
                )
                return processWechatUserInfo(wechatUserInfo, configId)
            }
            
            val wechatUserInfo = WechatUserInfo(
                openid = userInfoResponse["openid"] as? String ?: openid,
                unionid = userInfoResponse["unionid"] as? String ?: unionid,
                nickname = userInfoResponse["nickname"] as? String,
                headimgurl = userInfoResponse["headimgurl"] as? String,
                sex = (userInfoResponse["sex"] as? Number)?.toInt(),
                province = userInfoResponse["province"] as? String,
                city = userInfoResponse["city"] as? String,
                country = userInfoResponse["country"] as? String
            )
            
            processWechatUserInfo(wechatUserInfo, configId)
        } catch (e: Exception) {
            logger.error("Failed to handle OpenPlatform callback for $configId", e)
            null
        }
    }
    
    /**
     * 处理微信公众号回调
     */
    fun handleMpCallback(configId: String, code: String): TokenResponse? {
        val config = wechatConfigDao.findByConfigId(configId)
        if (config == null) {
            logger.warn("Wechat config not found: $configId")
            return null
        }
        
        return try {
            val mpService = wechatServiceFactory.getMpService(configId)
            if (mpService != null) {
                val oauth2Service = mpService.oAuth2Service
                val accessToken = oauth2Service.getAccessToken(code)
                val userInfo = oauth2Service.getUserInfo(accessToken, null)
                processWechatUser(userInfo, configId)
            } else {
                // 降级：使用 HTTP 请求直接调用微信 API
                handleMpWithHttpRequest(config, code)
            }
        } catch (e: Exception) {
            logger.error("Failed to handle MP callback for $configId", e)
            null
        }
    }
    
    /**
     * 处理微信小程序登录
     * 
     * 使用 jscode2session 接口获取 openid 和 session_key
     */
    fun handleMiniProgramLogin(configId: String, code: String): TokenResponse? {
        val config = wechatConfigDao.findByConfigId(configId)
        if (config == null) {
            logger.warn("Wechat config not found: $configId")
            return null
        }
        
        return try {
            // 调用微信小程序 jscode2session 接口
            val sessionUrl = "$MINI_JSCODE2SESSION_URL?" +
                    "appid=${config.appId}" +
                    "&secret=${config.appSecret}" +
                    "&js_code=$code" +
                    "&grant_type=authorization_code"
            
            val sessionResponse = restTemplate.getForObject(sessionUrl, Map::class.java)
            
            if (sessionResponse == null || sessionResponse.containsKey("errcode")) {
                val errcode = sessionResponse?.get("errcode")
                val errmsg = sessionResponse?.get("errmsg")
                logger.error("Failed to get session for mini program: errcode=$errcode, errmsg=$errmsg")
                return null
            }
            
            val openid = sessionResponse["openid"] as? String
            val unionid = sessionResponse["unionid"] as? String
            
            if (openid == null) {
                logger.error("Invalid session response: missing openid")
                return null
            }
            
            // 小程序只能获取 openid，用户信息需要前端授权后传递
            val wechatUserInfo = WechatUserInfo(
                openid = openid,
                unionid = unionid,
                nickname = null,
                headimgurl = null,
                sex = null,
                province = null,
                city = null,
                country = null
            )
            
            processWechatUserInfo(wechatUserInfo, configId)
        } catch (e: Exception) {
            logger.error("Failed to handle MiniProgram login for $configId", e)
            null
        }
    }
    
    /**
     * 处理微信用户信息（WxJava 格式）
     */
    private fun processWechatUser(userInfo: WxOAuth2UserInfo, configId: String): TokenResponse {
        val wechatUserInfo = WechatUserInfo(
            openid = userInfo.openid,
            unionid = userInfo.unionId,
            nickname = userInfo.nickname,
            headimgurl = userInfo.headImgUrl,
            sex = userInfo.sex,
            province = userInfo.province,
            city = userInfo.city,
            country = userInfo.country
        )
        
        return processWechatUserInfo(wechatUserInfo, configId)
    }
    
    /**
     * 处理微信用户信息
     */
    private fun processWechatUserInfo(wechatUserInfo: WechatUserInfo, configId: String): TokenResponse {
        // 生成用户名：优先使用 unionid，否则使用 openid
        val username = "wx_${wechatUserInfo.unionid ?: wechatUserInfo.openid}"
        
        logger.info("Processing Wechat user: $username, nickname=${wechatUserInfo.nickname}")
        
        // 查找或创建用户
        val userDetails = findOrCreateUser(username, wechatUserInfo)
        
        // 生成 JWT Token
        return generateTokenResponse(userDetails)
    }
    
    /**
     * 使用 HTTP 请求处理公众号回调（降级方案）
     * 
     * 当 WxMpService 不可用时使用
     */
    private fun handleMpWithHttpRequest(config: com.vgerbot.wechat.entity.WechatConfig, code: String): TokenResponse? {
        // 公众号网页授权使用与网站应用相同的 SNS API
        val tokenUrl = "$SNS_ACCESS_TOKEN_URL?" +
                "appid=${config.appId}" +
                "&secret=${config.appSecret}" +
                "&code=$code" +
                "&grant_type=authorization_code"
        
        val tokenResponse = restTemplate.getForObject(tokenUrl, Map::class.java)
        
        if (tokenResponse == null || tokenResponse.containsKey("errcode")) {
            val errcode = tokenResponse?.get("errcode")
            val errmsg = tokenResponse?.get("errmsg")
            logger.error("Failed to get access_token for MP: errcode=$errcode, errmsg=$errmsg")
            return null
        }
        
        val accessToken = tokenResponse["access_token"] as? String
        val openid = tokenResponse["openid"] as? String
        val unionid = tokenResponse["unionid"] as? String
        
        if (accessToken == null || openid == null) {
            logger.error("Invalid token response for MP: missing access_token or openid")
            return null
        }
        
        // 获取用户信息
        val userInfoUrl = "$SNS_USERINFO_URL?" +
                "access_token=$accessToken" +
                "&openid=$openid" +
                "&lang=zh_CN"
        
        val userInfoResponse = restTemplate.getForObject(userInfoUrl, Map::class.java)
        
        val wechatUserInfo = if (userInfoResponse != null && !userInfoResponse.containsKey("errcode")) {
            WechatUserInfo(
                openid = userInfoResponse["openid"] as? String ?: openid,
                unionid = userInfoResponse["unionid"] as? String ?: unionid,
                nickname = userInfoResponse["nickname"] as? String,
                headimgurl = userInfoResponse["headimgurl"] as? String,
                sex = (userInfoResponse["sex"] as? Number)?.toInt(),
                province = userInfoResponse["province"] as? String,
                city = userInfoResponse["city"] as? String,
                country = userInfoResponse["country"] as? String
            )
        } else {
            WechatUserInfo(
                openid = openid,
                unionid = unionid,
                nickname = null,
                headimgurl = null,
                sex = null,
                province = null,
                city = null,
                country = null
            )
        }
        
        return processWechatUserInfo(wechatUserInfo, config.configId)
    }
    
    /**
     * 查找或创建用户
     */
    private fun findOrCreateUser(username: String, wechatUserInfo: WechatUserInfo): ExtendedUserDetails {
        // 尝试查找用户
        var userInfo = userService.findUser(username)
        
        // 如果用户不存在，创建新用户
        if (userInfo == null) {
            val randomPassword = UUID.randomUUID().toString()
            
            val createUserDto = CreateUserDto(
                username = username,
                email = "$username@wechat.local",
                password = randomPassword,
                phoneNumber = null
            )
            
            val success = userService.createUser(createUserDto)
            if (success) {
                userInfo = userService.findUser(username)
                logger.info("Created new user from Wechat: $username")
            } else {
                logger.warn("Failed to create user from Wechat: $username")
                throw RuntimeException("Failed to create user")
            }
        }
        
        if (userInfo == null) {
            throw RuntimeException("User not found and creation failed")
        }
        
        return CustomUserDetails.fromUserInfo(userInfo, listOf("ROLE_USER"))
    }
    
    /**
     * 生成 Token 响应
     */
    private fun generateTokenResponse(userDetails: ExtendedUserDetails): TokenResponse {
        val accessToken = jwtTokenUtils.generateAccessToken(userDetails, userDetails.userId)
        val refreshToken = jwtTokenUtils.generateRefreshToken(userDetails, userDetails.userId)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = jwtProperties.accessTokenExpireTime,
            refreshExpiresIn = jwtProperties.refreshTokenExpireTime
        )
    }
}

