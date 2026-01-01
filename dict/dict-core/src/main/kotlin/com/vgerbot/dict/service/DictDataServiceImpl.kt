package com.vgerbot.dict.service

import com.vgerbot.dict.dao.DictDataDao
import com.vgerbot.dict.dao.DictTypeDao
import com.vgerbot.dict.dto.CreateDictDataDto
import com.vgerbot.dict.dto.DictDataDto
import com.vgerbot.dict.dto.UpdateDictDataDto
import com.vgerbot.dict.entity.DictData
import com.vgerbot.dict.entity.toDto
import com.vgerbot.dict.validation.DictValidator
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DictDataServiceImpl : DictDataService {
    
    @Autowired
    lateinit var dictDataDao: DictDataDao
    
    @Autowired
    lateinit var dictTypeDao: DictTypeDao
    
    @Autowired
    lateinit var dictValidator: DictValidator
    
    @Transactional
    override fun createDictData(dto: CreateDictDataDto): DictDataDto? {
        // 验证字典类型是否存在（启用的）
        val dictType = dictTypeDao.findOne { (it.id eq dto.dictTypeId) and (it.status eq 1) } ?: return null
        
        // 校验数据值
        dictValidator.validateOrThrow(dictType, dto.dataValue)
        
        // 检查同一字典编码下值是否已存在（只查询启用的）
        val existing = dictDataDao.findOne { 
            (it.dictCode eq dto.dictCode) and (it.dataValue eq dto.dataValue) and (it.status eq 1)
        }
        if (existing != null) {
            return null
        }
        
        val dictData = DictData()
        dictData.dictTypeId = dto.dictTypeId
        dictData.dictCode = dto.dictCode
        dictData.dataValue = dto.dataValue
        dictData.dataLabel = dto.dataLabel
        dictData.parentId = dto.parentId
        dictData.level = dto.level
        dictData.isDefault = dto.isDefault
        dictData.status = 1 // 默认启用
        dictData.sortOrder = dto.sortOrder
        dictData.remark = dto.remark
        dictData.createdAt = Instant.now()
        
        return if (dictDataDao.add(dictData) == 1) dictData.toDto() else null
    }
    
    @Transactional
    override fun updateDictData(id: Long, dto: UpdateDictDataDto): Boolean {
        val dictData = dictDataDao.findOne { it.id eq id } ?: return false
        
        // 如果更新了数据值，需要校验
        dto.dataValue?.let { newValue ->
            val dictType = dictTypeDao.findOne { it.id eq dictData.dictTypeId }
            if (dictType != null) {
                dictValidator.validateOrThrow(dictType, newValue)
            }
        }
        
        dto.dataValue?.let { dictData.dataValue = it }
        dto.dataLabel?.let { dictData.dataLabel = it }
        dto.parentId?.let { dictData.parentId = it }
        dto.level?.let { dictData.level = it }
        dto.isDefault?.let { dictData.isDefault = it }
        dto.sortOrder?.let { dictData.sortOrder = it }
        dto.remark?.let { dictData.remark = it }
        dictData.updatedAt = Instant.now()
        
        return dictDataDao.update(dictData) == 1
    }
    
    @Transactional
    override fun deleteDictData(id: Long): Boolean {
        val dictData = dictDataDao.findOne { it.id eq id } ?: return false
        dictData.status = 0 // 停用而不是删除
        dictData.updatedAt = Instant.now()
        return dictDataDao.update(dictData) == 1
    }
    
    override fun getDictDataById(id: Long): DictDataDto? {
        return dictDataDao.findOne { (it.id eq id) and (it.status eq 1) }?.toDto()
    }
    
    override fun getDictDataByCode(dictCode: String): List<DictDataDto> {
        return dictDataDao.findList { (it.dictCode eq dictCode) and (it.status eq 1) }.map { it.toDto() }
    }
    
    override fun getActiveDictDataByCode(dictCode: String): List<DictDataDto> {
        return dictDataDao.findList { (it.dictCode eq dictCode) and (it.status eq 1) }.map { it.toDto() }
    }
    
    override fun getDictDataTreeByCode(dictCode: String): List<DictDataDto> {
        val allData = dictDataDao.findList { (it.dictCode eq dictCode) and (it.status eq 1) }
        return buildTree(allData, 0)
    }
    
    override fun getDictDataByCodeAndParent(dictCode: String, parentId: Long): List<DictDataDto> {
        return dictDataDao.findList { (it.dictCode eq dictCode) and (it.parentId eq parentId) and (it.status eq 1) }.map { it.toDto() }
    }
    
    override fun getDictDataByTypeId(dictTypeId: Long): List<DictDataDto> {
        return dictDataDao.findList { (it.dictTypeId eq dictTypeId) and (it.status eq 1) }.map { it.toDto() }
    }
    
    override fun getDictDataByCodeAndValue(dictCode: String, dataValue: String): DictDataDto? {
        return dictDataDao.findOne { (it.dictCode eq dictCode) and (it.dataValue eq dataValue) and (it.status eq 1) }?.toDto()
    }
    
    override fun getDefaultDictDataByCode(dictCode: String): DictDataDto? {
        return dictDataDao.findOne { (it.dictCode eq dictCode) and (it.isDefault eq true) and (it.status eq 1) }?.toDto()
    }
    
    /**
     * 构建树形结构
     */
    private fun buildTree(allData: List<DictData>, parentId: Long): List<DictDataDto> {
        return allData
            .filter { it.parentId == parentId }
            .sortedBy { it.sortOrder }
            .map { data ->
                DictDataDto(
                    id = data.id,
                    dictTypeId = data.dictTypeId,
                    dictCode = data.dictCode,
                    dataValue = data.dataValue,
                    dataLabel = data.dataLabel,
                    parentId = data.parentId,
                    level = data.level,
                    isDefault = data.isDefault,
                    status = data.status == 1,
                    sortOrder = data.sortOrder,
                    createdBy = data.createdBy,
                    createdAt = data.createdAt,
                    updatedBy = data.updatedBy,
                    updatedAt = data.updatedAt,
                    remark = data.remark,
                    children = buildTree(allData, data.id)
                )
            }
    }
}


