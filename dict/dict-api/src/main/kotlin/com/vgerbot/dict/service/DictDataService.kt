package com.vgerbot.dict.service

import com.vgerbot.dict.dto.CreateDictDataDto
import com.vgerbot.dict.dto.DictDataDto
import com.vgerbot.dict.dto.UpdateDictDataDto
import com.vgerbot.dict.model.DictData

interface DictDataService {
    /**
     * 创建字典数据
     */
    fun createDictData(dto: CreateDictDataDto): DictData?
    
    /**
     * 更新字典数据
     */
    fun updateDictData(id: Long, dto: UpdateDictDataDto): Boolean
    
    /**
     * 删除字典数据
     */
    fun deleteDictData(id: Long): Boolean
    
    /**
     * 根据ID获取字典数据
     */
    fun getDictDataById(id: Long): DictData?
    
    /**
     * 根据字典编码获取所有字典数据
     */
    fun getDictDataByCode(dictCode: String): List<DictData>
    
    /**
     * 根据字典编码获取启用的字典数据
     */
    fun getActiveDictDataByCode(dictCode: String): List<DictData>
    
    /**
     * 根据字典编码获取树形结构字典数据
     */
    fun getDictDataTreeByCode(dictCode: String): List<DictDataDto>
    
    /**
     * 根据字典编码和父ID获取字典数据
     */
    fun getDictDataByCodeAndParent(dictCode: String, parentId: Long): List<DictData>
    
    /**
     * 根据字典类型ID获取字典数据
     */
    fun getDictDataByTypeId(dictTypeId: Long): List<DictData>
    
    /**
     * 根据字典编码和值获取字典数据
     */
    fun getDictDataByCodeAndValue(dictCode: String, dataValue: String): DictData?
    
    /**
     * 根据字典编码获取默认值
     */
    fun getDefaultDictDataByCode(dictCode: String): DictData?
}

