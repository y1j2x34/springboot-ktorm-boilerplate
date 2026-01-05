package com.vgerbot.wechat

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
import org.springframework.http.HttpStatus
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
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to configs
            )
        )
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
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config not found"
                )
            )
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to config
            )
        )
    }
    
    /**
     * 按登录类型获取配置
     */
    @GetMapping("type/{loginType}")
    fun getByLoginType(@PathVariable loginType: String): ResponseEntity<Map<String, Any>> {
        return try {
            val type = WechatLoginType.valueOf(loginType.uppercase())
            val configs = wechatConfigService.getByLoginType(type)
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "data" to configs
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to "Invalid login type: $loginType. Valid types: ${WechatLoginType.values().joinToString()}"
                )
            )
        }
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to "Invalid login type: ${dto.loginType}. Valid types: ${WechatLoginType.values().joinToString()}"
                )
            )
        }
        
        val result = wechatConfigService.create(dto.copy(loginType = dto.loginType.uppercase()))
            ?: return ResponseEntity.status(HttpStatus.CONFLICT).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config with this config ID already exists"
                )
            )
        
        logger.info("Created Wechat config: ${dto.configId}")
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "success" to true,
                "message" to "Wechat config created successfully",
                "data" to result
            )
        )
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
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config not found"
                )
            )
        
        logger.info("Updated Wechat config: $id")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Wechat config updated successfully",
                "data" to result
            )
        )
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "Wechat config not found"
                )
            )
        }
        
        logger.info("Deleted Wechat config: $id")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Wechat config deleted successfully"
            )
        )
    }
    
    @Operation(summary = "Refresh WeChat services", description = "Refresh WeChat service cache")
    @ApiResponse(responseCode = "200", description = "Services refreshed successfully")
    @PostMapping("refresh")
    fun refresh(): ResponseEntity<Map<String, Any>> {
        wechatConfigService.refreshWechatServices()
        
        logger.info("Refreshed Wechat services")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Wechat services refreshed successfully"
            )
        )
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
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to configs
            )
        )
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
        return try {
            val type = WechatLoginType.valueOf(loginType.uppercase())
            val configs = wechatConfigService.getByLoginType(type).map { config ->
                mapOf(
                    "config_id" to config.configId,
                    "name" to config.name,
                    "login_type" to config.loginType,
                    "login_url" to config.loginUrl
                )
            }
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "data" to configs
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "success" to false,
                    "message" to "Invalid login type: $loginType"
                )
            )
        }
    }
}

