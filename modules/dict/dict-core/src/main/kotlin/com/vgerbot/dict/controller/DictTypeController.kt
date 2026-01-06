package com.vgerbot.dict.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.DictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto
import com.vgerbot.dict.service.DictTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Dictionary Type Controller
 * Provides REST API for dictionary type management
 */
@Tag(name = "Dictionary Type", description = "Dictionary type management APIs")
@RestController
@RequestMapping("/dict/types")
class DictTypeController(
    private val dictTypeService: DictTypeService
) {
    
    @Operation(summary = "Create dictionary type", description = "Create a new dictionary type")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Dictionary type created successfully"),
        ApiResponse(responseCode = "409", description = "Dictionary type code already exists")
    )
    @PostMapping
    fun createDictType(
        @Parameter(description = "Dictionary type creation data", required = true)
        @RequestBody dto: CreateDictTypeDto
    ): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.createDictType(dto)
            ?: throw ConflictException("字典类型代码已存在")
        return dictType.created("字典类型创建成功")
    }
    
    @Operation(summary = "Update dictionary type", description = "Update an existing dictionary type")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary type updated successfully"),
        ApiResponse(responseCode = "404", description = "Dictionary type not found")
    )
    @PutMapping("/{id}")
    fun updateDictType(
        @Parameter(description = "Dictionary type ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Dictionary type update data", required = true)
        @RequestBody dto: UpdateDictTypeDto
    ): ResponseEntity<Map<String, Any>> {
        val updated = dictTypeService.updateDictType(id, dto)
        if (!updated) {
            throw NotFoundException("字典类型不存在")
        }
        return ok("字典类型更新成功")
    }
    
    @Operation(summary = "Delete dictionary type", description = "Delete a dictionary type")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary type deleted successfully"),
        ApiResponse(responseCode = "404", description = "Dictionary type not found")
    )
    @DeleteMapping("/{id}")
    fun deleteDictType(
        @Parameter(description = "Dictionary type ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val deleted = dictTypeService.deleteDictType(id)
        if (!deleted) {
            throw NotFoundException("字典类型不存在")
        }
        return ok("字典类型删除成功")
    }
    
    @Operation(summary = "Get dictionary type by ID", description = "Retrieve a dictionary type by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary type found"),
        ApiResponse(responseCode = "404", description = "Dictionary type not found")
    )
    @GetMapping("/{id}")
    fun getDictTypeById(
        @Parameter(description = "Dictionary type ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.getDictTypeById(id)
            ?: throw NotFoundException("字典类型不存在")
        return dictType.ok()
    }
    
    @Operation(summary = "Get dictionary type by code", description = "Retrieve a dictionary type by its code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary type found"),
        ApiResponse(responseCode = "404", description = "Dictionary type not found")
    )
    @GetMapping("/code/{code}")
    fun getDictTypeByCode(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.getDictTypeByCode(code)
            ?: throw NotFoundException("字典类型不存在")
        return dictType.ok()
    }
    
    @Operation(summary = "Get all dictionary types", description = "Retrieve all dictionary types, optionally filtered by category or status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionary types")
    @GetMapping
    fun getAllDictTypes(
        @Parameter(description = "Filter by category")
        @RequestParam(required = false) category: String?,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: Boolean?
    ): ResponseEntity<Map<String, Any>> {
        val dictTypes = when {
            category != null -> dictTypeService.getDictTypesByCategory(category)
            status != null -> dictTypeService.getDictTypesByStatus(status)
            else -> dictTypeService.getAllDictTypes()
        }
        return dictTypes.ok()
    }
}

