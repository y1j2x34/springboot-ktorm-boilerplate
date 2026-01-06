package com.vgerbot.dict.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.dict.dto.CreateDictDataDto
import com.vgerbot.dict.dto.DictDataDto
import com.vgerbot.dict.dto.UpdateDictDataDto
import com.vgerbot.dict.service.DictDataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Dictionary Data Controller
 * Provides REST API for dictionary data management
 */
@Tag(name = "Dictionary Data", description = "Dictionary data management APIs")
@RestController
@RequestMapping("/dict/data")
class DictDataController(
    private val dictDataService: DictDataService
) {
    
    @Operation(summary = "Create dictionary data", description = "Create a new dictionary data entry")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Dictionary data created successfully"),
        ApiResponse(responseCode = "409", description = "Dictionary data value already exists or dictionary type not found")
    )
    @PostMapping
    fun createDictData(
        @Parameter(description = "Dictionary data creation data", required = true)
        @RequestBody dto: CreateDictDataDto
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.createDictData(dto)
            ?: throw ConflictException("字典数据值已存在或字典类型不存在")
        return dictData.created("字典数据创建成功")
    }
    
    @Operation(summary = "Update dictionary data", description = "Update an existing dictionary data entry")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary data updated successfully"),
        ApiResponse(responseCode = "404", description = "Dictionary data not found")
    )
    @PutMapping("/{id}")
    fun updateDictData(
        @Parameter(description = "Dictionary data ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Dictionary data update data", required = true)
        @RequestBody dto: UpdateDictDataDto
    ): ResponseEntity<Map<String, Any>> {
        val updated = dictDataService.updateDictData(id, dto)
        if (!updated) {
            throw NotFoundException("字典数据不存在")
        }
        return ok("字典数据更新成功")
    }
    
    @Operation(summary = "Delete dictionary data", description = "Delete a dictionary data entry")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary data deleted successfully"),
        ApiResponse(responseCode = "404", description = "Dictionary data not found")
    )
    @DeleteMapping("/{id}")
    fun deleteDictData(
        @Parameter(description = "Dictionary data ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val deleted = dictDataService.deleteDictData(id)
        if (!deleted) {
            throw NotFoundException("字典数据不存在")
        }
        return ok("字典数据删除成功")
    }
    
    @Operation(summary = "Get dictionary data by ID", description = "Retrieve a dictionary data entry by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary data found"),
        ApiResponse(responseCode = "404", description = "Dictionary data not found")
    )
    @GetMapping("/{id}")
    fun getDictDataById(
        @Parameter(description = "Dictionary data ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.getDictDataById(id)
            ?: throw NotFoundException("字典数据不存在")
        return dictData.ok()
    }
    
    @Operation(summary = "Get dictionary data by code", description = "Retrieve dictionary data entries by dictionary type code")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionary data")
    @GetMapping("/code/{code}")
    fun getDictDataByCode(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String,
        @Parameter(description = "Return only active entries")
        @RequestParam(required = false) activeOnly: Boolean?
    ): ResponseEntity<Map<String, Any>> {
        val dictData = if (activeOnly == true) {
            dictDataService.getActiveDictDataByCode(code)
        } else {
            dictDataService.getDictDataByCode(code)
        }
        return dictData.ok()
    }
    
    @Operation(summary = "Get dictionary data tree by code", description = "Retrieve dictionary data entries as a tree structure by dictionary type code")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionary data tree")
    @GetMapping("/code/{code}/tree")
    fun getDictDataTreeByCode(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Map<String, Any>> {
        val dictDataTree = dictDataService.getDictDataTreeByCode(code)
        return dictDataTree.ok()
    }
    
    @Operation(summary = "Get dictionary data by code and parent", description = "Retrieve dictionary data entries by dictionary type code and parent ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionary data")
    @GetMapping("/code/{code}/parent/{parentId}")
    fun getDictDataByCodeAndParent(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String,
        @Parameter(description = "Parent ID", required = true)
        @PathVariable parentId: Long
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.getDictDataByCodeAndParent(code, parentId)
        return dictData.ok()
    }
    
    @Operation(summary = "Get dictionary data by type ID", description = "Retrieve dictionary data entries by dictionary type ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dictionary data")
    @GetMapping("/type/{typeId}")
    fun getDictDataByTypeId(
        @Parameter(description = "Dictionary type ID", required = true)
        @PathVariable typeId: Long
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.getDictDataByTypeId(typeId)
        return dictData.ok()
    }
    
    @Operation(summary = "Get dictionary data by code and value", description = "Retrieve a dictionary data entry by dictionary type code and value")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Dictionary data found"),
        ApiResponse(responseCode = "404", description = "Dictionary data not found")
    )
    @GetMapping("/code/{code}/value/{value}")
    fun getDictDataByCodeAndValue(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String,
        @Parameter(description = "Dictionary data value", required = true)
        @PathVariable value: String
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.getDictDataByCodeAndValue(code, value)
            ?: throw NotFoundException("字典数据不存在")
        return dictData.ok()
    }
    
    @Operation(summary = "Get default dictionary data by code", description = "Retrieve the default dictionary data entry by dictionary type code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Default dictionary data found"),
        ApiResponse(responseCode = "404", description = "Default dictionary data not found")
    )
    @GetMapping("/code/{code}/default")
    fun getDefaultDictDataByCode(
        @Parameter(description = "Dictionary type code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Map<String, Any>> {
        val dictData = dictDataService.getDefaultDictDataByCode(code)
            ?: throw NotFoundException("默认字典数据不存在")
        return dictData.ok()
    }
}

