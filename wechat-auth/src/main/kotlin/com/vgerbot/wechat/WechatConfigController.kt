package com.vgerbot.wechat

import com.vgerbot.wechat.dto.CreateWechatConfigDto
import com.vgerbot.wechat.dto.UpdateWechatConfigDto
import com.vgerbot.wechat.entity.WechatLoginType
import com.vgerbot.wechat.service.WechatConfigService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 微信配置管理控制器
 * 
 * 提供微信配置的 CRUD 操作（需要管理员权限）
 */
@RestController
@RequestMapping("admin/wechat/configs")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class WechatConfigController(
    private val wechatConfigService: WechatConfigService
) {
    
    private val logger = LoggerFactory.getLogger(WechatConfigController::class.java)
    
    /**
     * 获取所有微信配置
     */
    @GetMapping
    fun getAll(
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
    
    /**
     * 获取单个微信配置
     */
    @GetMapping("{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
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
    
    /**
     * 创建微信配置
     */
    @PostMapping
    fun create(@Valid @RequestBody dto: CreateWechatConfigDto): ResponseEntity<Map<String, Any>> {
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
    
    /**
     * 更新微信配置
     */
    @PutMapping("{id}")
    fun update(
        @PathVariable id: Int,
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
    
    /**
     * 删除微信配置（逻辑删除）
     */
    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
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
    
    /**
     * 刷新微信服务缓存
     */
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
 * 公开的微信配置列表接口（用于前端显示登录选项）
 */
@RestController
@RequestMapping("public/wechat/configs")
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class PublicWechatConfigController(
    private val wechatConfigService: WechatConfigService
) {
    
    /**
     * 获取所有启用的微信配置（仅返回公开信息）
     */
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
    
    /**
     * 按登录类型获取配置
     */
    @GetMapping("type/{loginType}")
    fun getByLoginType(@PathVariable loginType: String): ResponseEntity<Map<String, Any>> {
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

