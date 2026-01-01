package com.vgerbot.dict.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import com.vgerbot.dict.dto.DictDataDto
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface DictData : AuditableEntity<DictData> {
    companion object : Entity.Factory<DictData>()
    
    val id: Long
    var dictTypeId: Long
    var dictCode: String
    var dataValue: String
    var dataLabel: String
    var parentId: Long
    var level: Int
    var isDefault: Boolean
    var status: Boolean
    var sortOrder: Int
    var remark: String?
}

object DictDatas : AuditableTable<DictData>("dict_data") {
    val id = long("id").primaryKey().bindTo { it.id }
    val dictTypeId = long("dict_type_id").bindTo { it.dictTypeId }
    val dictCode = varchar("dict_code").bindTo { it.dictCode }
    val dataValue = varchar("data_value").bindTo { it.dataValue }
    val dataLabel = varchar("data_label").bindTo { it.dataLabel }
    val parentId = long("parent_id").bindTo { it.parentId }
    val level = int("level").bindTo { it.level }
    val isDefault = boolean("is_default").bindTo { it.isDefault }
    val status = boolean("status").bindTo { it.status }
    val sortOrder = int("sort_order").bindTo { it.sortOrder }
    val remark = varchar("remark").bindTo { it.remark }
}

val Database.dictDatas get() = this.sequenceOf(DictDatas)

fun DictData.toDto(): DictDataDto = DictDataDto(
    id = this.id,
    dictTypeId = this.dictTypeId,
    dictCode = this.dictCode,
    dataValue = this.dataValue,
    dataLabel = this.dataLabel,
    parentId = this.parentId,
    level = this.level,
    isDefault = this.isDefault,
    status = this.status,
    sortOrder = this.sortOrder,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt,
    remark = this.remark,
    children = null
)

