package com.vgerbot.oauth

import com.vgerbot.oauth.dto.CreateOAuth2ProviderDto
import com.vgerbot.oauth.dto.OAuth2ProviderResponseDto
import com.vgerbot.oauth.dto.UpdateOAuth2ProviderDto
import com.vgerbot.oauth.service.OAuth2ProviderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * OAuth2 Provider Controller
 * 
 * Provides CRUD operations for OAuth2 providers
 */
@Tag(name = "OAuth2 Provider", description = "OAuth2 provider management APIs")
@RestController
@RequestMapping("admin/oauth2/providers")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
class OAuth2ProviderController(
    private val oauth2ProviderService: OAuth2ProviderService
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2ProviderController::class.java)
    
    /**
     * Get all OAuth2 providers
     */
    @Operation(summary = "Get all OAuth2 providers", description = "Retrieve all OAuth2 providers, optionally including disabled ones")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved providers")
    @GetMapping
    fun getAll(
        @Parameter(description = "Include disabled providers")
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
    
    @Operation(summary = "Get OAuth2 provider by ID", description = "Retrieve a single OAuth2 provider by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Provider found"),
        ApiResponse(responseCode = "404", description = "Provider not found")
    )
    @GetMapping("{id}")
    fun getById(
        @Parameter(description = "Provider ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
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
    
    @Operation(summary = "Create OAuth2 provider", description = "Create a new OAuth2 provider")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Provider created successfully"),
        ApiResponse(responseCode = "409", description = "Provider with this registration ID already exists")
    )
    @PostMapping
    fun create(
        @Parameter(description = "Provider creation data", required = true)
        @Valid @RequestBody dto: CreateOAuth2ProviderDto
    ): ResponseEntity<Map<String, Any>> {
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
    
    @Operation(summary = "Update OAuth2 provider", description = "Update an existing OAuth2 provider")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Provider updated successfully"),
        ApiResponse(responseCode = "404", description = "Provider not found")
    )
    @PutMapping("{id}")
    fun update(
        @Parameter(description = "Provider ID", required = true)
        @PathVariable id: Int,
        @Parameter(description = "Provider update data", required = true)
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
    
    @Operation(summary = "Delete OAuth2 provider", description = "Delete an OAuth2 provider (soft delete)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Provider deleted successfully"),
        ApiResponse(responseCode = "404", description = "Provider not found")
    )
    @DeleteMapping("{id}")
    fun delete(
        @Parameter(description = "Provider ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
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
    
    @Operation(summary = "Refresh OAuth2 client registrations", description = "Refresh OAuth2 client registration cache")
    @ApiResponse(responseCode = "200", description = "Client registrations refreshed successfully")
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
 * Public OAuth2 Provider Controller
 * Public endpoint for listing enabled OAuth2 providers (for frontend login options)
 */
@Tag(name = "OAuth2 Provider", description = "OAuth2 provider management APIs")
@RestController
@RequestMapping("public/oauth2/providers")
class PublicOAuth2ProviderController(
    private val oauth2ProviderService: OAuth2ProviderService
) {
    
    /**
     * Get all enabled OAuth2 providers (public information only)
     */
    @Operation(summary = "Get enabled OAuth2 providers", description = "Retrieve all enabled OAuth2 providers (public information only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved providers")
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

