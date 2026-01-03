package com.vgerbot.dict.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.DictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto
import com.vgerbot.dict.service.DictTypeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dict/types")
class DictTypeController {
    
    @Autowired
    lateinit var dictTypeService: DictTypeService
    
    @PostMapping
    fun createDictType(@RequestBody dto: CreateDictTypeDto): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.createDictType(dto)
            ?: throw ConflictException("字典类型代码已存在")
        return dictType.created("字典类型创建成功")
    }
    
    @PutMapping("/{id}")
    fun updateDictType(@PathVariable id: Long, @RequestBody dto: UpdateDictTypeDto): ResponseEntity<Map<String, Any>> {
        val updated = dictTypeService.updateDictType(id, dto)
        if (!updated) {
            throw NotFoundException("字典类型不存在")
        }
        return ok("字典类型更新成功")
    }
    
    @DeleteMapping("/{id}")
    fun deleteDictType(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val deleted = dictTypeService.deleteDictType(id)
        if (!deleted) {
            throw NotFoundException("字典类型不存在")
        }
        return ok("字典类型删除成功")
    }
    
    @GetMapping("/{id}")
    fun getDictTypeById(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.getDictTypeById(id)
            ?: throw NotFoundException("字典类型不存在")
        return dictType.ok()
    }
    
    @GetMapping("/code/{code}")
    fun getDictTypeByCode(@PathVariable code: String): ResponseEntity<Map<String, Any>> {
        val dictType = dictTypeService.getDictTypeByCode(code)
            ?: throw NotFoundException("字典类型不存在")
        return dictType.ok()
    }
    
    @GetMapping
    fun getAllDictTypes(@RequestParam(required = false) category: String?, 
                       @RequestParam(required = false) status: Boolean?): ResponseEntity<Map<String, Any>> {
        val dictTypes = when {
            category != null -> dictTypeService.getDictTypesByCategory(category)
            status != null -> dictTypeService.getDictTypesByStatus(status)
            else -> dictTypeService.getAllDictTypes()
        }
        return dictTypes.ok()
    }
}

