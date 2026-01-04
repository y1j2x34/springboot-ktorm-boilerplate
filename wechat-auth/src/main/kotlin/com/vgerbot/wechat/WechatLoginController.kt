package com.vgerbot.wechat

import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.wechat.dto.WechatLoginCallbackRequest
import com.vgerbot.wechat.wechat.WechatLoginService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 微信登录控制器
 * 
 * 处理微信开放平台、公众号、小程序登录
 */
@RestController
@RequestMapping("public/wechat")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class WechatLoginController(
    private val wechatLoginService: WechatLoginService
) {
    
    private val logger = LoggerFactory.getLogger(WechatLoginController::class.java)
    
    // ==================== 微信开放平台（PC 扫码登录）====================
    
    /**
     * 获取微信开放平台授权 URL
     * 
     * 前端获取此 URL 后，可以：
     * 1. 重定向到此 URL 让用户扫码
     * 2. 或者使用微信 JS-SDK 在页面内嵌二维码
     */
    @GetMapping("open/{configId}/auth-url")
    fun getOpenPlatformAuthUrl(
        @PathVariable configId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(required = false) state: String?
    ): ResponseEntity<Map<String, Any>> {
        val authUrl = wechatLoginService.generateOpenPlatformAuthUrl(configId, redirectUri, state)
        
        if (authUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config not found or invalid type"
                )
            )
        }
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to mapOf(
                    "auth_url" to authUrl
                )
            )
        )
    }
    
    /**
     * 微信开放平台重定向登录
     * 
     * 直接重定向到微信授权页面
     */
    @GetMapping("open/{configId}/login")
    fun openPlatformLogin(
        @PathVariable configId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(required = false) state: String?,
        response: HttpServletResponse
    ) {
        val authUrl = wechatLoginService.generateOpenPlatformAuthUrl(configId, redirectUri, state)
        
        if (authUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wechat config not found")
            return
        }
        
        response.sendRedirect(authUrl)
    }
    
    /**
     * 微信开放平台回调
     */
    @GetMapping("open/{configId}/callback")
    fun openPlatformCallback(
        @PathVariable configId: String,
        @RequestParam code: String,
        @RequestParam(required = false) state: String?
    ): ResponseEntity<Map<String, Any>> {
        logger.info("OpenPlatform callback: configId=$configId, code=$code, state=$state")
        
        return try {
            val tokenResponse = wechatLoginService.handleOpenPlatformCallback(configId, code)
            
            if (tokenResponse == null) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Wechat login failed"
                    )
                )
            } else {
                logger.info("OpenPlatform login successful for config: $configId")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Wechat login successful",
                        "data" to tokenResponse
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("OpenPlatform callback failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat login failed: ${e.message}"
                )
            )
        }
    }
    
    // ==================== 微信公众号（微信内 H5 登录）====================
    
    /**
     * 获取微信公众号授权 URL
     */
    @GetMapping("mp/{configId}/auth-url")
    fun getMpAuthUrl(
        @PathVariable configId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false, defaultValue = "snsapi_userinfo") scope: String
    ): ResponseEntity<Map<String, Any>> {
        val authUrl = wechatLoginService.generateMpAuthUrl(configId, redirectUri, state, scope)
        
        if (authUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config not found or invalid type"
                )
            )
        }
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to mapOf(
                    "auth_url" to authUrl
                )
            )
        )
    }
    
    /**
     * 微信公众号重定向登录
     */
    @GetMapping("mp/{configId}/login")
    fun mpLogin(
        @PathVariable configId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false, defaultValue = "snsapi_userinfo") scope: String,
        response: HttpServletResponse
    ) {
        val authUrl = wechatLoginService.generateMpAuthUrl(configId, redirectUri, state, scope)
        
        if (authUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wechat config not found")
            return
        }
        
        response.sendRedirect(authUrl)
    }
    
    /**
     * 微信公众号回调
     */
    @GetMapping("mp/{configId}/callback")
    fun mpCallback(
        @PathVariable configId: String,
        @RequestParam code: String,
        @RequestParam(required = false) state: String?
    ): ResponseEntity<Map<String, Any>> {
        logger.info("MP callback: configId=$configId, code=$code, state=$state")
        
        return try {
            val tokenResponse = wechatLoginService.handleMpCallback(configId, code)
            
            if (tokenResponse == null) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Wechat login failed"
                    )
                )
            } else {
                logger.info("MP login successful for config: $configId")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Wechat login successful",
                        "data" to tokenResponse
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("MP callback failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat login failed: ${e.message}"
                )
            )
        }
    }
    
    // ==================== 微信小程序 ====================
    
    /**
     * 微信小程序登录
     * 
     * 小程序端使用 wx.login() 获取 code 后调用此接口
     */
    @PostMapping("mini/{configId}/login")
    fun miniProgramLogin(
        @PathVariable configId: String,
        @RequestBody request: WechatLoginCallbackRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("MiniProgram login: configId=$configId")
        
        return try {
            val tokenResponse = wechatLoginService.handleMiniProgramLogin(configId, request.code)
            
            if (tokenResponse == null) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "Wechat login failed"
                    )
                )
            } else {
                logger.info("MiniProgram login successful for config: $configId")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Wechat login successful",
                        "data" to tokenResponse
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("MiniProgram login failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat login failed: ${e.message}"
                )
            )
        }
    }
}

