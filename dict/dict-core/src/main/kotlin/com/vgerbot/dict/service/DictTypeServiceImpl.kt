package com.vgerbot.dict.service

import com.vgerbot.dict.dao.DictTypeDao
import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto
import com.vgerbot.dict.entity.DictType
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class DictTypeServiceImpl : DictTypeService {
    
    @Autowired
    lateinit var dictTypeDao: DictTypeDao
    
    @Transactional
    override fun createDictType(dto: CreateDictTypeDto): DictTypeDto? {
        // 检查字典编码是否已存在
        val existing = dictTypeDao.findOne { it.dictCode eq dto.dictCode }
        if (existing != null) {
            return null
        }
        
        val dictType = DictType()
        dictType.dictCode = dto.dictCode
        dictType.dictName = dto.dictName
        dictType.dictCategory = dto.dictCategory
        dictType.valueType = dto.valueType
        dictType.validationRule = dto.validationRule
        dictType.validationMessage = dto.validationMessage
        dictType.isTree = dto.isTree
        dictType.description = dto.description
        dictType.status = dto.status
        dictType.sortOrder = dto.sortOrder
        dictType.remark = dto.remark
        dictType.createdTime = LocalDateTime.now()
        dictType.updatedTime = LocalDateTime.now()
        
        return if (dictTypeDao.add(dictType) == 1) dictType.toDto() else null
    }
    
    @Transactional
    override fun updateDictType(id: Long, dto: UpdateDictTypeDto): Boolean {
        val dictType = dictTypeDao.findOne { it.id eq id } ?: return false
        
        dto.dictCode?.let { dictType.dictCode = it }
        dto.dictName?.let { dictType.dictName = it }
        dto.dictCategory?.let { dictType.dictCategory = it }
        dto.valueType?.let { dictType.valueType = it }
        dto.validationRule?.let { dictType.validationRule = it }
        dto.validationMessage?.let { dictType.validationMessage = it }
        dto.isTree?.let { dictType.isTree = it }
        dto.description?.let { dictType.description = it }
        dto.status?.let { dictType.status = it }
        dto.sortOrder?.let { dictType.sortOrder = it }
        dto.remark?.let { dictType.remark = it }
        dictType.updatedTime = LocalDateTime.now()
        
        return dictTypeDao.update(dictType) == 1
    }
    
    @Transactional
    override fun deleteDictType(id: Long): Boolean {
        return dictTypeDao.deleteIf { it.id eq id } == 1
    }
    
    override fun getDictTypeById(id: Long): DictTypeDto? {
        return dictTypeDao.findOne { it.id eq id }?.toDto()
    }
    
    override fun getDictTypeByCode(dictCode: String): DictTypeDto? {
        return dictTypeDao.findOne { it.dictCode eq dictCode }?.toDto()
    }
    
    override fun getAllDictTypes(): List<DictTypeDto> {
        return dictTypeDao.findAll().map { it.toDto() }
    }
    
    override fun getDictTypesByCategory(category: String): List<DictTypeDto> {
        return dictTypeDao.findList { it.dictCategory eq category }.map { it.toDto() }
    }
    
    override fun getDictTypesByStatus(status: Boolean): List<DictTypeDto> {
        return dictTypeDao.findList { it.status eq status }.map { it.toDto() }
    }
}

