package com.vgerbot.oauth2server.controller

import com.vgerbot.oauth2server.data.OAuth2ClientCreateRequest
import com.vgerbot.oauth2server.data.OAuth2ClientResponse
import com.vgerbot.oauth2server.data.OAuth2ClientUpdateRequest
import com.vgerbot.oauth2server.service.OAuth2ClientService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * OAuth2 客户端管理控制器
 */
@RestController
@RequestMapping("/api/admin/oauth2/clients")
class OAuth2ClientController(
    private val oauth2ClientService: OAuth2ClientService
) {

    /**
     * 创建 OAuth2 客户端
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createClient(@RequestBody request: OAuth2ClientCreateRequest): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.createClient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(client)
    }

    /**
     * 更新 OAuth2 客户端
     */
    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateClient(
        @PathVariable clientId: String,
        @RequestBody request: OAuth2ClientUpdateRequest
    ): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.updateClient(clientId, request)
        return ResponseEntity.ok(client)
    }

    /**
     * 删除 OAuth2 客户端
     */
    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteClient(@PathVariable clientId: String): ResponseEntity<Void> {
        oauth2ClientService.deleteClient(clientId)
        return ResponseEntity.noContent().build()
    }

    /**
     * 根据客户端 ID 获取客户端信息
     */
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClient(@PathVariable clientId: String): ResponseEntity<OAuth2ClientResponse> {
        val client = oauth2ClientService.findByClientId(clientId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(client)
    }

    /**
     * 获取所有客户端列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllClients(): ResponseEntity<List<OAuth2ClientResponse>> {
        val clients = oauth2ClientService.findAll()
        return ResponseEntity.ok(clients)
    }

    /**
     * 获取所有启用的客户端列表
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    fun getEnabledClients(): ResponseEntity<List<OAuth2ClientResponse>> {
        val clients = oauth2ClientService.findAllEnabled()
        return ResponseEntity.ok(clients)
    }
}

