package com.vgerbot.dict.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

interface DictType : Entity<DictType> {
    companion object : Entity.Factory<DictType>()
    
    val id: Long
    var dictCode: String
    var dictName: String
    var dictCategory: String?
    var valueType: String
    var validationRule: String?
    var validationMessage: String?
    var isTree: Boolean
    var description: String?
    var status: Boolean
    var sortOrder: Int
    var createdBy: Int?
    var createdTime: LocalDateTime
    var updatedBy: Int?
    var updatedTime: LocalDateTime
    var remark: String?
}

object DictTypes : Table<DictType>("dict_type") {
    val id = long("id").primaryKey().bindTo { it.id }
    val dictCode = varchar("dict_code").bindTo { it.dictCode }
    val dictName = varchar("dict_name").bindTo { it.dictName }
    val dictCategory = varchar("dict_category").bindTo { it.dictCategory }
    val valueType = varchar("value_type").bindTo { it.valueType }
    val validationRule = varchar("validation_rule").bindTo { it.validationRule }
    val validationMessage = varchar("validation_message").bindTo { it.validationMessage }
    val isTree = boolean("is_tree").bindTo { it.isTree }
    val description = text("description").bindTo { it.description }
    val status = boolean("status").bindTo { it.status }
    val sortOrder = int("sort_order").bindTo { it.sortOrder }
    val createdBy = int("created_by").bindTo { it.createdBy }
    val createdTime = datetime("created_time").bindTo { it.createdTime }
    val updatedBy = int("updated_by").bindTo { it.updatedBy }
    val updatedTime = datetime("updated_time").bindTo { it.updatedTime }
    val remark = varchar("remark").bindTo { it.remark }
}

val Database.dictTypes get() = this.sequenceOf(DictTypes)

