package com.vgerbot.dict.service

import com.vgerbot.dict.dao.DictTypeDao
import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.DictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto
import com.vgerbot.dict.entity.DictType
import com.vgerbot.dict.entity.toDto
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DictTypeServiceImpl : DictTypeService {
    
    @Autowired
    lateinit var dictTypeDao: DictTypeDao
    
    @Transactional
    override fun createDictType(dto: CreateDictTypeDto): DictTypeDto? {
        // 检查字典编码是否已存在（只查询启用的）
        val existing = dictTypeDao.findOne { (it.dictCode eq dto.dictCode) and (it.status eq 1) }
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
        dictType.status = 1 // 默认启用
        dictType.sortOrder = dto.sortOrder
        dictType.remark = dto.remark
        dictType.createdAt = Instant.now()
        
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
        dto.sortOrder?.let { dictType.sortOrder = it }
        dto.remark?.let { dictType.remark = it }
        dictType.updatedAt = Instant.now()
        
        return dictTypeDao.update(dictType) == 1
    }
    
    @Transactional
    override fun deleteDictType(id: Long): Boolean {
        val dictType = dictTypeDao.findOne { it.id eq id } ?: return false
        dictType.status = 0 // 停用而不是删除
        dictType.updatedAt = Instant.now()
        return dictTypeDao.update(dictType) == 1
    }
    
    override fun getDictTypeById(id: Long): DictTypeDto? {
        return dictTypeDao.findOne { (it.id eq id) and (it.status eq 1) }?.toDto()
    }
    
    override fun getDictTypeByCode(dictCode: String): DictTypeDto? {
        return dictTypeDao.findOne { (it.dictCode eq dictCode) and (it.status eq 1) }?.toDto()
    }
    
    override fun getAllDictTypes(): List<DictTypeDto> {
        return dictTypeDao.findList { it.status eq 1 }.map { it.toDto() }
    }
    
    override fun getDictTypesByCategory(category: String): List<DictTypeDto> {
        return dictTypeDao.findList { (it.dictCategory eq category) and (it.status eq 1) }.map { it.toDto() }
    }
    
    override fun getDictTypesByStatus(status: Boolean): List<DictTypeDto> {
        return dictTypeDao.findList { it.status eq if (status) 1 else 0 }.map { it.toDto() }
    }
}


