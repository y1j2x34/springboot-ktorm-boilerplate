package com.vgerbot.oauth2server.controller

import com.vgerbot.oauth2server.data.OAuth2ClientCreateRequest
import com.vgerbot.oauth2server.data.OAuth2ClientResponse
import com.vgerbot.oauth2server.data.OAuth2ClientUpdateRequest
import com.vgerbot.oauth2server.service.OAuth2ClientService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * OAuth2 Client Controller
 * Provides REST API for OAuth2 client management
 */
@Tag(name = "OAuth2 Client", description = "OAuth2 client management APIs")
@RestController
@RequestMapping("/api/admin/oauth2/clients")
@SecurityRequirement(name = "bearer-jwt")
class OAuth2ClientController(
    private val oauth2ClientService: OAuth2ClientService
) {

    /**
     * Create OAuth2 client
     */
    @Operation(summary = "Create OAuth2 client", description = "Create a new OAuth2 client")
    @ApiResponse(responseCode = "201", description = "Client created successfully")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createClient(
        @Parameter(description = "Client creation data", required = true)
        @RequestBody request: OAuth2ClientCreateRequest
    ): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.createClient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(client)
    }

    @Operation(summary = "Update OAuth2 client", description = "Update an existing OAuth2 client")
    @ApiResponse(responseCode = "200", description = "Client updated successfully")
    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateClient(
        @Parameter(description = "Client ID", required = true)
        @PathVariable clientId: String,
        @Parameter(description = "Client update data", required = true)
        @RequestBody request: OAuth2ClientUpdateRequest
    ): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.updateClient(clientId, request)
        return ResponseEntity.ok(client)
    }

    @Operation(summary = "Delete OAuth2 client", description = "Delete an OAuth2 client")
    @ApiResponse(responseCode = "204", description = "Client deleted successfully")
    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteClient(
        @Parameter(description = "Client ID", required = true)
        @PathVariable clientId: String
    ): ResponseEntity<Void> {
        oauth2ClientService.deleteClient(clientId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Get OAuth2 client by ID", description = "Retrieve an OAuth2 client by its client ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Client found"),
        ApiResponse(responseCode = "404", description = "Client not found")
    )
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClient(
        @Parameter(description = "Client ID", required = true)
        @PathVariable clientId: String
    ): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.findByClientId(clientId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(client)
    }

    @Operation(summary = "Get all OAuth2 clients", description = "Retrieve all OAuth2 clients")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved clients")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllClients(): ResponseEntity<List<OAuth2ClientResponse>> {
        val clients = oauth2ClientService.findAll()
        return ResponseEntity.ok(clients)
    }

    @Operation(summary = "Get enabled OAuth2 clients", description = "Retrieve all enabled OAuth2 clients")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enabled clients")
    @GetMapping("/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    fun getEnabledClients(): ResponseEntity<List<OAuth2ClientResponse>> {
        val clients = oauth2ClientService.findAllEnabled()
        return ResponseEntity.ok(clients)
    }
}

