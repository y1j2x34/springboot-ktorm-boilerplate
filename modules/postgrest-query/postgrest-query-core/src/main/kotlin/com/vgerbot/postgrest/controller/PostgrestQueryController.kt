package com.vgerbot.postgrest.controller

import com.vgerbot.common.controller.*
import com.vgerbot.postgrest.api.PostgrestQueryService
import com.vgerbot.postgrest.dto.QueryRequest
import com.vgerbot.postgrest.dto.QueryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import kotlin.reflect.full.memberProperties

/**
 * PostgREST 查询控制器
 */
@Tag(name = "PostgREST Query", description = "PostgREST 风格的动态查询 API")
@RestController
@RequestMapping("/api/postgrest")
class PostgrestQueryController(
    private val queryService: PostgrestQueryService
) {
    
    private val logger = LoggerFactory.getLogger(PostgrestQueryController::class.java)
    
    /**
     * 执行查询请求
     */
    @Operation(
        summary = "执行 PostgREST 查询",
        description = "根据 JSON 配置执行动态查询，支持 SELECT、INSERT、UPDATE、DELETE、UPSERT 操作"
    )
    @PostMapping("/query")
    fun executeQuery(
        @Valid @RequestBody request: QueryRequest
    ): org.springframework.http.ResponseEntity<Map<String, Any>> {
        logger.info("Received query request: {}", request)
        
        // 获取当前用户信息
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw com.vgerbot.common.exception.UnauthorizedException("用户未认证")
        
        val userId = getUserId(authentication)
        val tenantId = getTenantId(authentication)
        
        // 执行查询
        val response = queryService.executeQuery(request, userId, tenantId)
        
        return response.ok()
    }
    
    /**
     * 从认证对象中获取用户ID
     */
    private fun getUserId(authentication: org.springframework.security.core.Authentication): String {
        val principal = authentication.principal
        
        // 尝试通过反射获取 userId 字段（ExtendedUserDetails）
        try {
            val userIdField = principal::class.java.getDeclaredField("userId")
            userIdField.isAccessible = true
            val userIdValue = userIdField.get(principal)
            if (userIdValue != null) {
                return userIdValue.toString()
            }
        } catch (e: Exception) {
            // 继续尝试其他方法
        }
        
        // 尝试通过反射获取 id 字段
        try {
            val idField = principal::class.java.getDeclaredField("id")
            idField.isAccessible = true
            val idValue = idField.get(principal)
            if (idValue != null) {
                return idValue.toString()
            }
        } catch (e: Exception) {
            // 继续尝试其他方法
        }
        
        // 如果 principal 是字符串，直接返回
        if (principal is String) {
            return principal
        }
        
        // 尝试通过反射获取 id 属性（Kotlin 属性）
        try {
            val idProperty = principal::class.memberProperties.find { it.name == "userId" || it.name == "id" }
            if (idProperty != null) {
                val idValue = idProperty.call(principal)
                if (idValue != null) {
                    return idValue.toString()
                }
            }
        } catch (e: Exception) {
            logger.warn("Cannot extract user id from principal: {}", principal::class.java.name)
        }
        
        return authentication.name
    }
    
    /**
     * 从认证对象中获取租户ID
     */
    private fun getTenantId(authentication: org.springframework.security.core.Authentication): String? {
        val principal = authentication.principal
        
        // 尝试通过反射获取 tenantId 字段
        try {
            val tenantIdProperty = principal::class.memberProperties.find { it.name == "tenantId" }
            if (tenantIdProperty != null) {
                val tenantIdValue = tenantIdProperty.call(principal)
                return tenantIdValue?.toString()
            }
        } catch (e: Exception) {
            // 没有 tenantId 字段，返回 null
        }
        
        return null
    }
}

