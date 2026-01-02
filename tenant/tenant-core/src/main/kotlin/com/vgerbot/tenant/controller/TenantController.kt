package com.vgerbot.tenant.controller

import com.vgerbot.tenant.dto.TenantDto
import com.vgerbot.tenant.service.TenantService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 租户控制器
 * 提供租户管理的 REST API
 */
@RestController
@RequestMapping("/api/tenants")
class TenantController {
    
    @Autowired
    private lateinit var tenantService: TenantService
    
    /**
     * 获取所有租户
     */
    @GetMapping
    fun getAllTenants(): ResponseEntity<List<TenantDto>> {
        val tenants = tenantService.getAllTenants()
        return ResponseEntity.ok(tenants)
    }
    
    /**
     * 根据 ID 获取租户
     */
    @GetMapping("/{id}")
    fun getTenantById(@PathVariable id: Int): ResponseEntity<Any> {
        val tenant = tenantService.getTenantById(id)
        return if (tenant != null) {
            ResponseEntity.ok(tenant)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Tenant not found"))
        }
    }
    
    /**
     * 根据 code 获取租户
     */
    @GetMapping("/code/{code}")
    fun getTenantByCode(@PathVariable code: String): ResponseEntity<Any> {
        val tenant = tenantService.getTenantByCode(code)
        return if (tenant != null) {
            ResponseEntity.ok(tenant)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Tenant not found"))
        }
    }
    
    /**
     * 根据邮箱域名查找匹配的租户
     */
    @GetMapping("/find-by-email")
    fun findByEmail(@RequestParam email: String): ResponseEntity<Any> {
        if (email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Email parameter is required"))
        }

        val tenant = tenantService.findTenantByEmail(email)
        return if (tenant != null) {
            ResponseEntity.ok(tenant)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "No matching tenant found for email"))
        }
    }
    
    /**
     * 获取用户的所有租户
     */
    @GetMapping("/user/{userId}")
    fun getTenantsForUser(@PathVariable userId: Int): ResponseEntity<List<TenantDto>> {
        val tenants = tenantService.getTenantsForUser(userId)
        return ResponseEntity.ok(tenants)
    }
}
