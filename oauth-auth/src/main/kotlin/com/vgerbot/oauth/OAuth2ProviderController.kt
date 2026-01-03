package com.vgerbot.oauth

import com.vgerbot.oauth.dto.CreateOAuth2ProviderDto
import com.vgerbot.oauth.dto.OAuth2ProviderResponseDto
import com.vgerbot.oauth.dto.UpdateOAuth2ProviderDto
import com.vgerbot.oauth.service.OAuth2ProviderService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * OAuth2 Provider 管理控制器
 * 
 * 提供 OAuth2 提供商的 CRUD 操作
 */
@RestController
@RequestMapping("admin/oauth2/providers")
@PreAuthorize("hasRole('ADMIN')")
class OAuth2ProviderController(
    private val oauth2ProviderService: OAuth2ProviderService
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2ProviderController::class.java)
    
    /**
     * 获取所有 OAuth2 提供商
     */
    @GetMapping
    fun getAll(
        @RequestParam(required = false, defaultValue = "false") includeDisabled: Boolean
    ): ResponseEntity<Map<String, Any>> {
        val providers = if (includeDisabled) {
            oauth2ProviderService.getAll()
        } else {
            oauth2ProviderService.getAllEnabled()
        }
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to providers
            )
        )
    }
    
    /**
     * 获取单个 OAuth2 提供商
     */
    @GetMapping("{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val provider = oauth2ProviderService.getById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "OAuth2 provider not found"
                )
            )
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to provider
            )
        )
    }
    
    /**
     * 创建 OAuth2 提供商
     */
    @PostMapping
    fun create(@Valid @RequestBody dto: CreateOAuth2ProviderDto): ResponseEntity<Map<String, Any>> {
        val result = oauth2ProviderService.create(dto)
            ?: return ResponseEntity.status(HttpStatus.CONFLICT).body(
                mapOf(
                    "success" to false,
                    "message" to "OAuth2 provider with this registration ID already exists"
                )
            )
        
        logger.info("Created OAuth2 provider: ${dto.registrationId}")
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "success" to true,
                "message" to "OAuth2 provider created successfully",
                "data" to result
            )
        )
    }
    
    /**
     * 更新 OAuth2 提供商
     */
    @PutMapping("{id}")
    fun update(
        @PathVariable id: Int,
        @Valid @RequestBody dto: UpdateOAuth2ProviderDto
    ): ResponseEntity<Map<String, Any>> {
        val result = oauth2ProviderService.update(id, dto)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "OAuth2 provider not found"
                )
            )
        
        logger.info("Updated OAuth2 provider: $id")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "OAuth2 provider updated successfully",
                "data" to result
            )
        )
    }
    
    /**
     * 删除 OAuth2 提供商（逻辑删除）
     */
    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val result = oauth2ProviderService.delete(id)
        
        if (!result) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "success" to false,
                    "message" to "OAuth2 provider not found"
                )
            )
        }
        
        logger.info("Deleted OAuth2 provider: $id")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "OAuth2 provider deleted successfully"
            )
        )
    }
    
    /**
     * 刷新 OAuth2 客户端注册缓存
     */
    @PostMapping("refresh")
    fun refresh(): ResponseEntity<Map<String, Any>> {
        oauth2ProviderService.refreshClientRegistrations()
        
        logger.info("Refreshed OAuth2 client registrations")
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "OAuth2 client registrations refreshed successfully"
            )
        )
    }
}

/**
 * 公开的 OAuth2 提供商列表接口（用于前端显示登录选项）
 */
@RestController
@RequestMapping("public/oauth2/providers")
class PublicOAuth2ProviderController(
    private val oauth2ProviderService: OAuth2ProviderService
) {
    
    /**
     * 获取所有启用的 OAuth2 提供商（仅返回公开信息）
     */
    @GetMapping
    fun getEnabledProviders(): ResponseEntity<Map<String, Any>> {
        val providers = oauth2ProviderService.getAllEnabled().map { provider ->
            mapOf(
                "registration_id" to provider.registrationId,
                "name" to provider.name,
                "login_url" to "/login/oauth2/authorization/${provider.registrationId}"
            )
        }
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to providers
            )
        )
    }
}

