package com.vgerbot.dict.controller

import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.DictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto
import com.vgerbot.dict.service.DictTypeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dict/types")
class DictTypeController {
    
    @Autowired
    lateinit var dictTypeService: DictTypeService
    
    @PostMapping
    fun createDictType(@RequestBody dto: CreateDictTypeDto): ResponseEntity<Any> {
        val dictType = dictTypeService.createDictType(dto)
        return if (dictType != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(dictType)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Dictionary type code already exists"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateDictType(@PathVariable id: Long, @RequestBody dto: UpdateDictTypeDto): ResponseEntity<Any> {
        val updated = dictTypeService.updateDictType(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Dictionary type updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary type not found"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteDictType(@PathVariable id: Long): ResponseEntity<Any> {
        val deleted = dictTypeService.deleteDictType(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Dictionary type deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary type not found"))
        }
    }
    
    @GetMapping("/{id}")
    fun getDictTypeById(@PathVariable id: Long): ResponseEntity<Any> {
        val dictType = dictTypeService.getDictTypeById(id)
        return if (dictType != null) {
            ResponseEntity.ok(dictType)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary type not found"))
        }
    }
    
    @GetMapping("/code/{code}")
    fun getDictTypeByCode(@PathVariable code: String): ResponseEntity<Any> {
        val dictType = dictTypeService.getDictTypeByCode(code)
        return if (dictType != null) {
            ResponseEntity.ok(dictType)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary type not found"))
        }
    }
    
    @GetMapping
    fun getAllDictTypes(@RequestParam(required = false) category: String?, 
                       @RequestParam(required = false) status: Boolean?): ResponseEntity<List<DictTypeDto>> {
        val dictTypes = when {
            category != null -> dictTypeService.getDictTypesByCategory(category)
            status != null -> dictTypeService.getDictTypesByStatus(status)
            else -> dictTypeService.getAllDictTypes()
        }
        return ResponseEntity.ok(dictTypes)
    }
}

