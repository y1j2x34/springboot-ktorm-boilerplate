package com.vgerbot.wechat

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.common.exception.ValidationException
import com.vgerbot.wechat.dto.CreateWechatConfigDto
import com.vgerbot.wechat.dto.UpdateWechatConfigDto
import com.vgerbot.wechat.entity.WechatLoginType
import com.vgerbot.wechat.service.WechatConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * WeChat Config Controller
 * 
 * Provides CRUD operations for WeChat configurations (requires admin role)
 */
@Tag(name = "WeChat Config", description = "WeChat configuration management APIs")
@RestController
@RequestMapping("admin/wechat/configs")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
@SecurityRequirement(name = "bearer-jwt")
class WechatConfigController(
    private val wechatConfigService: WechatConfigService
) {
    
    private val logger = LoggerFactory.getLogger(WechatConfigController::class.java)
    
    /**
     * Get all WeChat configurations
     */
    @Operation(summary = "Get all WeChat configs", description = "Retrieve all WeChat configurations, optionally including disabled ones")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved configs")
    @GetMapping
    fun getAll(
        @Parameter(description = "Include disabled configs")
        @RequestParam(required = false, defaultValue = "false") includeDisabled: Boolean
    ): ResponseEntity<Map<String, Any>> {
        val configs = if (includeDisabled) {
            wechatConfigService.getAll()
        } else {
            wechatConfigService.getAllEnabled()
        }
        
        return configs.ok()
    }
    
    @Operation(summary = "Get WeChat config by ID", description = "Retrieve a single WeChat configuration by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Config found"),
        ApiResponse(responseCode = "404", description = "Config not found")
    )
    @GetMapping("{id}")
    fun getById(
        @Parameter(description = "Config ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
        val config = wechatConfigService.getById(id)
            ?: throw NotFoundException("微信配置不存在")
        
        return config.ok()
    }
    
    /**
     * 按登录类型获取配置
     */
    @GetMapping("type/{loginType}")
    fun getByLoginType(@PathVariable loginType: String): ResponseEntity<Map<String, Any>> {
        val type = try {
            WechatLoginType.valueOf(loginType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ValidationException(
                "无效的登录类型: $loginType. 有效类型: ${WechatLoginType.values().joinToString()}",
                "loginType"
            )
        }
        
        val configs = wechatConfigService.getByLoginType(type)
        return configs.ok()
    }
    
    @Operation(summary = "Create WeChat config", description = "Create a new WeChat configuration")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Config created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid login type"),
        ApiResponse(responseCode = "409", description = "Config with this config ID already exists")
    )
    @PostMapping
    fun create(
        @Parameter(description = "Config creation data", required = true)
        @Valid @RequestBody dto: CreateWechatConfigDto
    ): ResponseEntity<Map<String, Any>> {
        // 验证登录类型
        try {
            WechatLoginType.valueOf(dto.loginType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ValidationException(
                "无效的登录类型: ${dto.loginType}. 有效类型: ${WechatLoginType.values().joinToString()}",
                "loginType"
            )
        }
        
        val result = wechatConfigService.create(dto.copy(loginType = dto.loginType.uppercase()))
            ?: throw ConflictException("微信配置ID已存在")
        
        logger.info("Created Wechat config: ${dto.configId}")
        
        return result.created("微信配置创建成功")
    }
    
    @Operation(summary = "Update WeChat config", description = "Update an existing WeChat configuration")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Config updated successfully"),
        ApiResponse(responseCode = "404", description = "Config not found")
    )
    @PutMapping("{id}")
    fun update(
        @Parameter(description = "Config ID", required = true)
        @PathVariable id: Int,
        @Parameter(description = "Config update data", required = true)
        @Valid @RequestBody dto: UpdateWechatConfigDto
    ): ResponseEntity<Map<String, Any>> {
        val result = wechatConfigService.update(id, dto)
            ?: throw NotFoundException("微信配置不存在")
        
        logger.info("Updated Wechat config: $id")
        
        return result.ok("微信配置更新成功")
    }
    
    @Operation(summary = "Delete WeChat config", description = "Delete a WeChat configuration (soft delete)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Config deleted successfully"),
        ApiResponse(responseCode = "404", description = "Config not found")
    )
    @DeleteMapping("{id}")
    fun delete(
        @Parameter(description = "Config ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
        val result = wechatConfigService.delete(id)
        
        if (!result) {
            throw NotFoundException("微信配置不存在")
        }
        
        logger.info("Deleted Wechat config: $id")
        
        return ok("微信配置删除成功")
    }
    
    @Operation(summary = "Refresh WeChat services", description = "Refresh WeChat service cache")
    @ApiResponse(responseCode = "200", description = "Services refreshed successfully")
    @PostMapping("refresh")
    fun refresh(): ResponseEntity<Map<String, Any>> {
        wechatConfigService.refreshWechatServices()
        
        logger.info("Refreshed Wechat services")
        
        return ok("微信服务刷新成功")
    }
}

/**
 * Public WeChat Config Controller
 * Public endpoint for listing enabled WeChat configurations (for frontend login options)
 */
@Tag(name = "WeChat Config", description = "WeChat configuration management APIs")
@RestController
@RequestMapping("public/wechat/configs")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class PublicWechatConfigController(
    private val wechatConfigService: WechatConfigService
) {
    
    /**
     * Get all enabled WeChat configurations (public information only)
     */
    @Operation(summary = "Get enabled WeChat configs", description = "Retrieve all enabled WeChat configurations (public information only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved configs")
    @GetMapping
    fun getEnabledConfigs(): ResponseEntity<Map<String, Any>> {
        val configs = wechatConfigService.getAllEnabled().map { config ->
            mapOf(
                "config_id" to config.configId,
                "name" to config.name,
                "login_type" to config.loginType,
                "login_url" to config.loginUrl
            )
        }
        
        return configs.ok()
    }
    
    @Operation(summary = "Get WeChat configs by login type", description = "Retrieve enabled WeChat configurations by login type")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved configs"),
        ApiResponse(responseCode = "400", description = "Invalid login type")
    )
    @GetMapping("type/{loginType}")
    fun getByLoginType(
        @Parameter(description = "Login type", required = true)
        @PathVariable loginType: String
    ): ResponseEntity<Map<String, Any>> {
        val type = try {
            WechatLoginType.valueOf(loginType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ValidationException("无效的登录类型: $loginType", "loginType")
        }
        
        val configs = wechatConfigService.getByLoginType(type).map { config ->
            mapOf(
                "config_id" to config.configId,
                "name" to config.name,
                "login_type" to config.loginType,
                "login_url" to config.loginUrl
            )
        }
        
        return configs.ok()
    }
}

