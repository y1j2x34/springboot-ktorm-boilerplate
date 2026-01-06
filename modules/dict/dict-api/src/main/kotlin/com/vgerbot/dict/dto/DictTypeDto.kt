package com.vgerbot.dict.dto

import java.time.Instant

data class DictTypeDto(
    val id: Long? = null,
    val dictCode: String,
    val dictName: String,
    val dictCategory: String? = null,
    val valueType: String = "STRING",
    val validationRule: String? = null,
    val validationMessage: String? = null,
    val isTree: Boolean = false,
    val description: String? = null,
    val status: Boolean = true,
    val sortOrder: Int = 0,
    val createdBy: Int? = null,
    val createdAt: Instant? = null,
    val updatedBy: Int? = null,
    val updatedAt: Instant? = null,
    val remark: String? = null
)

data class CreateDictTypeDto(
    val dictCode: String,
    val dictName: String,
    val dictCategory: String? = null,
    val valueType: String = "STRING",
    val validationRule: String? = null,
    val validationMessage: String? = null,
    val isTree: Boolean = false,
    val description: String? = null,
    val status: Boolean = true,
    val sortOrder: Int = 0,
    val remark: String? = null
)

data class UpdateDictTypeDto(
    val dictCode: String? = null,
    val dictName: String? = null,
    val dictCategory: String? = null,
    val valueType: String? = null,
    val validationRule: String? = null,
    val validationMessage: String? = null,
    val isTree: Boolean? = null,
    val description: String? = null,
    val status: Boolean? = null,
    val sortOrder: Int? = null,
    val remark: String? = null
)

