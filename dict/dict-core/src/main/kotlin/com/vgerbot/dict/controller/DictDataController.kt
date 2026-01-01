package com.vgerbot.dict.controller

import com.vgerbot.dict.dto.CreateDictDataDto
import com.vgerbot.dict.dto.DictDataDto
import com.vgerbot.dict.dto.UpdateDictDataDto
import com.vgerbot.dict.service.DictDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dict/data")
class DictDataController {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    @PostMapping
    fun createDictData(@RequestBody dto: CreateDictDataDto): ResponseEntity<Any> {
        val dictData = dictDataService.createDictData(dto)
        return if (dictData != null) {
            ResponseEntity.status(HttpStatus.CREATED).body(dictData)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Dictionary data value already exists or dictionary type not found"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateDictData(@PathVariable id: Long, @RequestBody dto: UpdateDictDataDto): ResponseEntity<Any> {
        val updated = dictDataService.updateDictData(id, dto)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Dictionary data updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary data not found"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteDictData(@PathVariable id: Long): ResponseEntity<Any> {
        val deleted = dictDataService.deleteDictData(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Dictionary data deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary data not found"))
        }
    }
    
    @GetMapping("/{id}")
    fun getDictDataById(@PathVariable id: Long): ResponseEntity<Any> {
        val dictData = dictDataService.getDictDataById(id)
        return if (dictData != null) {
            ResponseEntity.ok(dictData)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary data not found"))
        }
    }
    
    @GetMapping("/code/{code}")
    fun getDictDataByCode(@PathVariable code: String, 
                         @RequestParam(required = false) activeOnly: Boolean?): ResponseEntity<List<DictDataDto>> {
        val dictData = if (activeOnly == true) {
            dictDataService.getActiveDictDataByCode(code)
        } else {
            dictDataService.getDictDataByCode(code)
        }
        return ResponseEntity.ok(dictData)
    }
    
    @GetMapping("/code/{code}/tree")
    fun getDictDataTreeByCode(@PathVariable code: String): ResponseEntity<List<DictDataDto>> {
        val dictDataTree = dictDataService.getDictDataTreeByCode(code)
        return ResponseEntity.ok(dictDataTree)
    }
    
    @GetMapping("/code/{code}/parent/{parentId}")
    fun getDictDataByCodeAndParent(@PathVariable code: String, 
                                   @PathVariable parentId: Long): ResponseEntity<List<DictDataDto>> {
        val dictData = dictDataService.getDictDataByCodeAndParent(code, parentId)
        return ResponseEntity.ok(dictData)
    }
    
    @GetMapping("/type/{typeId}")
    fun getDictDataByTypeId(@PathVariable typeId: Long): ResponseEntity<List<DictDataDto>> {
        val dictData = dictDataService.getDictDataByTypeId(typeId)
        return ResponseEntity.ok(dictData)
    }
    
    @GetMapping("/code/{code}/value/{value}")
    fun getDictDataByCodeAndValue(@PathVariable code: String, 
                                  @PathVariable value: String): ResponseEntity<Any> {
        val dictData = dictDataService.getDictDataByCodeAndValue(code, value)
        return if (dictData != null) {
            ResponseEntity.ok(dictData)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Dictionary data not found"))
        }
    }
    
    @GetMapping("/code/{code}/default")
    fun getDefaultDictDataByCode(@PathVariable code: String): ResponseEntity<Any> {
        val dictData = dictDataService.getDefaultDictDataByCode(code)
        return if (dictData != null) {
            ResponseEntity.ok(dictData)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Default dictionary data not found"))
        }
    }
}

