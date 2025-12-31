package com.vgerbot.dict.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

interface DictData : Entity<DictData> {
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
    var createdBy: Int?
    var createdTime: LocalDateTime
    var updatedBy: Int?
    var updatedTime: LocalDateTime
    var remark: String?
}

object DictDatas : Table<DictData>("dict_data") {
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
    val createdBy = int("created_by").bindTo { it.createdBy }
    val createdTime = datetime("created_time").bindTo { it.createdTime }
    val updatedBy = int("updated_by").bindTo { it.updatedBy }
    val updatedTime = datetime("updated_time").bindTo { it.updatedTime }
    val remark = varchar("remark").bindTo { it.remark }
}

val Database.dictDatas get() = this.sequenceOf(DictDatas)

