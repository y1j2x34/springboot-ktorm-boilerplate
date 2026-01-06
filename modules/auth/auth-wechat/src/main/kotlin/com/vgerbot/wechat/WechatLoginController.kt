package com.vgerbot.wechat

import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.wechat.dto.WechatLoginCallbackRequest
import com.vgerbot.wechat.wechat.WechatLoginService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * WeChat Login Controller
 * 
 * Handles WeChat Open Platform, Official Account, and Mini Program login
 */
@Tag(name = "WeChat Login", description = "WeChat authentication APIs")
@RestController
@RequestMapping("public/wechat")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class WechatLoginController(
    private val wechatLoginService: WechatLoginService
) {
    
    private val logger = LoggerFactory.getLogger(WechatLoginController::class.java)
    
    // ==================== 微信开放平台（PC 扫码登录）====================
    
    /**
     * Get WeChat Open Platform authorization URL
     * 
     * After getting this URL, frontend can:
     * 1. Redirect to this URL for user to scan QR code
     * 2. Or use WeChat JS-SDK to embed QR code in page
     */
    @Operation(summary = "Get WeChat Open Platform auth URL", description = "Get authorization URL for WeChat Open Platform login")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Auth URL generated successfully"),
        ApiResponse(responseCode = "404", description = "WeChat config not found or invalid type")
    )
    @GetMapping("open/{configId}/auth-url")
    fun getOpenPlatformAuthUrl(
        @Parameter(description = "WeChat config ID", required = true)
        @PathVariable configId: String,
        @Parameter(description = "Redirect URI after authorization", required = true)
        @RequestParam("redirect_uri") redirectUri: String,
        @Parameter(description = "Optional state parameter")
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
     * WeChat Open Platform redirect login
     * 
     * Directly redirect to WeChat authorization page
     */
    @Operation(summary = "WeChat Open Platform redirect login", description = "Redirect to WeChat authorization page for Open Platform login")
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
    
    @Operation(summary = "WeChat Open Platform callback", description = "Callback endpoint for WeChat Open Platform authorization")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "WeChat login successful"),
        ApiResponse(responseCode = "401", description = "WeChat login failed"),
        ApiResponse(responseCode = "500", description = "WeChat login error")
    )
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
    
    @Operation(summary = "Get WeChat Official Account auth URL", description = "Get authorization URL for WeChat Official Account login")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Auth URL generated successfully"),
        ApiResponse(responseCode = "404", description = "WeChat config not found or invalid type")
    )
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
    
    @Operation(summary = "WeChat Official Account redirect login", description = "Redirect to WeChat authorization page for Official Account login")
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
    
    @Operation(summary = "WeChat Official Account callback", description = "Callback endpoint for WeChat Official Account authorization")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "WeChat login successful"),
        ApiResponse(responseCode = "401", description = "WeChat login failed"),
        ApiResponse(responseCode = "500", description = "WeChat login error")
    )
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
     * WeChat Mini Program login
     * 
     * Mini Program calls this endpoint after getting code from wx.login()
     */
    @Operation(summary = "WeChat Mini Program login", description = "Login using WeChat Mini Program code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "WeChat login successful"),
        ApiResponse(responseCode = "401", description = "WeChat login failed"),
        ApiResponse(responseCode = "500", description = "WeChat login error")
    )
    @PostMapping("mini/{configId}/login")
    fun miniProgramLogin(
        @Parameter(description = "WeChat config ID", required = true)
        @PathVariable configId: String,
        @Parameter(description = "Login request with code", required = true)
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

