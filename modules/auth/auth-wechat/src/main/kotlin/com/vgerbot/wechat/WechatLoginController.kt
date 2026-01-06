package com.vgerbot.wechat

import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.NotFoundException
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
            ?: throw NotFoundException("微信配置不存在或类型无效")
        
        return mapOf("auth_url" to authUrl).ok()
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
        
        val tokenResponse = wechatLoginService.handleOpenPlatformCallback(configId, code)
            ?: throw com.vgerbot.common.exception.UnauthorizedException("微信登录失败")
        
        logger.info("OpenPlatform login successful for config: $configId")
        return tokenResponse.ok("微信登录成功")
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
            ?: throw NotFoundException("微信配置不存在或类型无效")
        
        return mapOf("auth_url" to authUrl).ok()
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
        
        val tokenResponse = wechatLoginService.handleMpCallback(configId, code)
            ?: throw com.vgerbot.common.exception.UnauthorizedException("微信登录失败")
        
        logger.info("MP login successful for config: $configId")
        return tokenResponse.ok("微信登录成功")
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
        
        val tokenResponse = wechatLoginService.handleMiniProgramLogin(configId, request.code)
            ?: throw com.vgerbot.common.exception.UnauthorizedException("微信登录失败")
        
        logger.info("MiniProgram login successful for config: $configId")
        return tokenResponse.ok("微信登录成功")
    }
}

