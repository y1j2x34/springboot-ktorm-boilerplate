package com.vgerbot.dict.example

import com.vgerbot.dict.dto.CreateDictDataDto
import com.vgerbot.dict.dto.CreateDictTypeDto
import com.vgerbot.dict.service.DictDataService
import com.vgerbot.dict.service.DictTypeService
import com.vgerbot.dict.validation.DictValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 字典数据校验使用示例
 */
@Component
class DictValidationExample {
    
    @Autowired
    lateinit var dictTypeService: DictTypeService
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    /**
     * 示例1: 正则表达式校验
     * 场景：用户名只能包含字母和数字
     */
    fun regexValidationExample() {
        // 创建字典类型，配置正则校验规则
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "username_format",
                dictName = "用户名格式",
                valueType = "STRING",
                validationRule = """{"type": "regex", "pattern": "^[a-zA-Z0-9]+$"}""",
                validationMessage = "用户名只能包含字母和数字",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 正确的值
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "username_format",
                        dataValue = "user123",
                        dataLabel = "有效用户名"
                    )
                )
                println("✓ 校验通过: user123")
                
                // 错误的值（包含特殊字符）
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "username_format",
                        dataValue = "user@123",
                        dataLabel = "无效用户名"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例2: 数值范围校验
     * 场景：年龄必须在 0-150 之间
     */
    fun rangeValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "age_range",
                dictName = "年龄范围",
                valueType = "INTEGER",
                validationRule = """{"type": "range", "min": 0, "max": 150}""",
                validationMessage = "年龄必须在0-150岁之间",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 有效年龄
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "age_range",
                        dataValue = "25",
                        dataLabel = "青年"
                    )
                )
                println("✓ 校验通过: 25")
                
                // 无效年龄
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "age_range",
                        dataValue = "200",
                        dataLabel = "无效年龄"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例3: 长度校验
     * 场景：验证码必须是6位
     */
    fun lengthValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "verification_code",
                dictName = "验证码",
                valueType = "STRING",
                validationRule = """{"type": "length", "minLength": 6, "maxLength": 6}""",
                validationMessage = "验证码必须是6位",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 正确长度
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "verification_code",
                        dataValue = "123456",
                        dataLabel = "有效验证码"
                    )
                )
                println("✓ 校验通过: 123456")
                
                // 错误长度
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "verification_code",
                        dataValue = "123",
                        dataLabel = "无效验证码"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例4: 枚举值校验
     * 场景：性别只能是特定值
     */
    fun enumValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "gender_strict",
                dictName = "性别（严格）",
                valueType = "STRING",
                validationRule = """{"type": "enum", "values": ["M", "F", "U"]}""",
                validationMessage = "性别值必须是 M（男）、F（女）或 U（未知）",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 有效值
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "gender_strict",
                        dataValue = "M",
                        dataLabel = "男"
                    )
                )
                println("✓ 校验通过: M")
                
                // 无效值
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "gender_strict",
                        dataValue = "male",
                        dataLabel = "男性"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例5: 邮箱校验
     * 场景：邮箱格式验证
     */
    fun emailValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "contact_email",
                dictName = "联系邮箱",
                valueType = "STRING",
                validationRule = """{"type": "email"}""",
                validationMessage = "请输入有效的邮箱地址",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 有效邮箱
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "contact_email",
                        dataValue = "user@example.com",
                        dataLabel = "用户邮箱"
                    )
                )
                println("✓ 校验通过: user@example.com")
                
                // 无效邮箱
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "contact_email",
                        dataValue = "invalid-email",
                        dataLabel = "无效邮箱"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例6: 手机号校验
     * 场景：中国大陆手机号验证
     */
    fun phoneValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "phone_number",
                dictName = "手机号",
                valueType = "STRING",
                validationRule = """{"type": "phone"}""",
                validationMessage = "请输入有效的手机号码",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 有效手机号
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "phone_number",
                        dataValue = "13800138000",
                        dataLabel = "联系电话"
                    )
                )
                println("✓ 校验通过: 13800138000")
                
                // 无效手机号
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "phone_number",
                        dataValue = "12345",
                        dataLabel = "错误号码"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
    
    /**
     * 示例7: 数字类型校验（正整数）
     */
    fun numberValidationExample() {
        val dictType = dictTypeService.createDictType(
            CreateDictTypeDto(
                dictCode = "quantity",
                dictName = "数量",
                valueType = "INTEGER",
                validationRule = """{"type": "number", "integerOnly": true, "positive": true}""",
                validationMessage = "数量必须是正整数",
                status = true
            )
        )
        
        if (dictType != null) {
            try {
                // 有效数量
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "quantity",
                        dataValue = "10",
                        dataLabel = "10件"
                    )
                )
                println("✓ 校验通过: 10")
                
                // 无效数量（负数）
                dictDataService.createDictData(
                    CreateDictDataDto(
                        dictTypeId = dictType.id,
                        dictCode = "quantity",
                        dataValue = "-5",
                        dataLabel = "负数"
                    )
                )
            } catch (e: DictValidationException) {
                println("✗ 校验失败: ${e.message}")
            }
        }
    }
}

