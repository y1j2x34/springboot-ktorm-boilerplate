package com.vgerbot.dict.example

import com.vgerbot.dict.dto.*
import com.vgerbot.dict.service.DictDataService
import com.vgerbot.dict.service.DictTypeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 字典模块使用示例
 * 
 * 此类展示了字典模块的基本使用方法，包括：
 * 1. 创建简单字典
 * 2. 创建树形字典
 * 3. 查询字典数据
 * 4. 在业务中使用字典
 */
@Component
class DictUsageExample {
    
    @Autowired
    lateinit var dictTypeService: DictTypeService
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    /**
     * 示例1: 创建简单字典（性别）
     */
    fun createSimpleDictExample() {
        // 1. 创建字典类型
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "gender",
                dictName = "性别",
                dictCategory = "common",
                valueType = "STRING",
                isTree = false,
                description = "性别类型字典",
                status = true,
                sortOrder = 1
            )
        )
        
        if (dictType != null) {
            // 2. 添加字典数据项
            val genderValues = listOf(
                CreateDictDataDto(
                    dictTypeId = dictType.id,
                    dictCode = "gender",
                    dataValue = "male",
                    dataLabel = "男",
                    isDefault = false,
                    status = true,
                    sortOrder = 1
                ),
                CreateDictDataDto(
                    dictTypeId = dictType.id,
                    dictCode = "gender",
                    dataValue = "female",
                    dataLabel = "女",
                    isDefault = false,
                    status = true,
                    sortOrder = 2
                ),
                CreateDictDataDto(
                    dictTypeId = dictType.id,
                    dictCode = "gender",
                    dataValue = "unknown",
                    dataLabel = "未知",
                    isDefault = true,
                    status = true,
                    sortOrder = 3
                )
            )
            
            genderValues.forEach { dto ->
                dictDataService.createDictData(dto)
            }
        }
    }
    
    /**
     * 示例2: 创建树形字典（地区）
     */
    fun createTreeDictExample() {
        // 1. 创建树形字典类型
        val regionType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "region",
                dictName = "地区",
                dictCategory = "common",
                valueType = "STRING",
                isTree = true,  // 启用树形结构
                description = "行政区域树形字典",
                status = true,
                sortOrder = 1
            )
        )
        
        if (regionType != null) {
            // 2. 添加省级数据
            val guangdong = dictDataService.createDictData(
                CreateDictDataDto(
                    dictTypeId = regionType.id,
                    dictCode = "region",
                    dataValue = "440000",
                    dataLabel = "广东省",
                    parentId = 0,  // 根节点
                    level = 1,
                    status = true,
                    sortOrder = 1
                )
            )
            
            // 3. 添加市级数据
            if (guangdong != null) {
                listOf(
                    CreateDictDataDto(
                        dictTypeId = regionType.id,
                        dictCode = "region",
                        dataValue = "440100",
                        dataLabel = "广州市",
                        parentId = guangdong.id,
                        level = 2,
                        status = true,
                        sortOrder = 1
                    ),
                    CreateDictDataDto(
                        dictTypeId = regionType.id,
                        dictCode = "region",
                        dataValue = "440300",
                        dataLabel = "深圳市",
                        parentId = guangdong.id,
                        level = 2,
                        status = true,
                        sortOrder = 2
                    )
                ).forEach { dto ->
                    dictDataService.createDictData(dto)
                }
            }
        }
    }
    
    /**
     * 示例3: 查询字典数据
     */
    fun queryDictExample() {
        // 查询所有性别选项
        val genderList = dictDataService.getActiveDictDataByCode("gender")
        println("性别选项：")
        genderList.forEach { 
            println("  ${it.dataValue} - ${it.dataLabel}")
        }
        
        // 获取默认性别
        val defaultGender = dictDataService.getDefaultDictDataByCode("gender")
        println("默认性别：${defaultGender?.dataLabel}")
        
        // 根据值查询标签
        val genderLabel = dictDataService.getDictDataByCodeAndValue("gender", "male")
        println("male 对应的标签：${genderLabel?.dataLabel}")
        
        // 查询树形地区数据
        val regionTree = dictDataService.getDictDataTreeByCode("region")
        println("地区树形结构：")
        printTree(regionTree, 0)
    }
    
    /**
     * 示例4: 在业务中使用字典
     */
    fun useInBusinessExample() {
        // 场景：用户注册时选择性别
        val userGender = "male"
        
        // 验证性别值是否有效
        val genderData = dictDataService.getDictDataByCodeAndValue("gender", userGender)
        if (genderData != null && genderData.status) {
            println("用户性别有效：${genderData.dataLabel}")
            // 保存用户信息...
        } else {
            println("无效的性别值")
        }
        
        // 场景：获取所有可用的用户状态供前端选择
        val statusOptions = dictDataService.getActiveDictDataByCode("user_status")
        val statusList = statusOptions.map { 
            mapOf("value" to it.dataValue, "label" to it.dataLabel) 
        }
        println("用户状态选项：$statusList")
        
        // 场景：级联选择地区（先选省，再选市）
        val selectedProvince = "440000"  // 广东省
        val cities = dictDataService.getDictDataByCodeAndParent("region", 
            dictDataService.getDictDataByCodeAndValue("region", selectedProvince)?.id ?: 0
        )
        println("广东省下辖城市：")
        cities.forEach { println("  ${it.dataLabel}") }
    }
    
    /**
     * 示例5: 更新和删除字典
     */
    fun updateAndDeleteExample() {
        // 更新字典类型
        val dictType = dictTypeService.getDictTypeByCode("gender")
        if (dictType != null) {
            dictTypeService.updateDictType(
                dictType.id,
                UpdateDictTypeDto(
                    dictName = "性别（更新）",
                    description = "更新后的性别字典描述"
                )
            )
        }
        
        // 更新字典数据
        val maleData = dictDataService.getDictDataByCodeAndValue("gender", "male")
        if (maleData != null) {
            dictDataService.updateDictData(
                maleData.id,
                UpdateDictDataDto(
                    dataLabel = "男性",
                    sortOrder = 10
                )
            )
        }
        
        // 停用某个字典数据（软删除）
        val unknownData = dictDataService.getDictDataByCodeAndValue("gender", "unknown")
        if (unknownData != null) {
            dictDataService.updateDictData(
                unknownData.id,
                UpdateDictDataDto(status = false)
            )
        }
        
        // 删除字典数据（真删除，需谨慎）
        // dictDataService.deleteDictData(unknownData.id)
    }
    
    /**
     * 辅助方法：打印树形结构
     */
    private fun printTree(nodes: List<DictDataDto>, level: Int) {
        val indent = "  ".repeat(level)
        nodes.forEach { node ->
            println("$indent${node.dataLabel} (${node.dataValue})")
            node.children?.let { 
                printTree(it, level + 1) 
            }
        }
    }
}

