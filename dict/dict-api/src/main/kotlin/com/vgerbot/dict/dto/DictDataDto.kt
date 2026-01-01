package com.vgerbot.dict.dto

import java.time.Instant

data class DictDataDto(
    val id: Long? = null,
    val dictTypeId: Long,
    val dictCode: String,
    val dataValue: String,
    val dataLabel: String,
    val parentId: Long = 0,
    val level: Int = 1,
    val isDefault: Boolean = false,
    val status: Boolean = true,
    val sortOrder: Int = 0,
    val createdBy: Int? = null,
    val createdAt: Instant? = null,
    val updatedBy: Int? = null,
    val updatedAt: Instant? = null,
    val remark: String? = null,
    val children: List<DictDataDto>? = null
)

data class CreateDictDataDto(
    val dictTypeId: Long,
    val dictCode: String,
    val dataValue: String,
    val dataLabel: String,
    val parentId: Long = 0,
    val level: Int = 1,
    val isDefault: Boolean = false,
    val status: Boolean = true,
    val sortOrder: Int = 0,
    val remark: String? = null
)

data class UpdateDictDataDto(
    val dataValue: String? = null,
    val dataLabel: String? = null,
    val parentId: Long? = null,
    val level: Int? = null,
    val isDefault: Boolean? = null,
    val status: Boolean? = null,
    val sortOrder: Int? = null,
    val remark: String? = null
)

data class DictDataQueryDto(
    val dictCode: String,
    val parentId: Long? = null,
    val status: Boolean? = true
)

