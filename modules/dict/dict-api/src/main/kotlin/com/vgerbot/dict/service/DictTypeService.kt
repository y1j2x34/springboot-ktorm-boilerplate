package com.vgerbot.dict.service

import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.dto.DictTypeDto
import com.vgerbot.dict.dto.UpdateDictTypeDto

interface DictTypeService {
    /**
     * 创建字典类型
     */
    fun createDictType(dto: CreateDictTypeDto): DictTypeDto?
    
    /**
     * 更新字典类型
     */
    fun updateDictType(id: Long, dto: UpdateDictTypeDto): Boolean
    
    /**
     * 删除字典类型
     */
    fun deleteDictType(id: Long): Boolean
    
    /**
     * 根据ID获取字典类型
     */
    fun getDictTypeById(id: Long): DictTypeDto?
    
    /**
     * 根据字典编码获取字典类型
     */
    fun getDictTypeByCode(dictCode: String): DictTypeDto?
    
    /**
     * 获取所有字典类型
     */
    fun getAllDictTypes(): List<DictTypeDto>
    
    /**
     * 根据分类获取字典类型
     */
    fun getDictTypesByCategory(category: String): List<DictTypeDto>
    
    /**
     * 根据状态获取字典类型
     */
    fun getDictTypesByStatus(status: Boolean): List<DictTypeDto>
}

