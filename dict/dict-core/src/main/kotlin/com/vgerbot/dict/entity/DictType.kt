package com.vgerbot.dict.entity

import com.vgerbot.common.entity.AuditableEntity
import com.vgerbot.common.entity.AuditableTable
import com.vgerbot.dict.dto.DictTypeDto
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

interface DictType : AuditableEntity<DictType> {
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
    var remark: String?
}

object DictTypes : AuditableTable<DictType>("dict_type") {
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
    val remark = varchar("remark").bindTo { it.remark }
}

val Database.dictTypes get() = this.sequenceOf(DictTypes)

fun DictType.toDto(): DictTypeDto = DictTypeDto(
    id = this.id,
    dictCode = this.dictCode,
    dictName = this.dictName,
    dictCategory = this.dictCategory,
    valueType = this.valueType,
    validationRule = this.validationRule,
    validationMessage = this.validationMessage,
    isTree = this.isTree,
    description = this.description,
    status = this.status,
    sortOrder = this.sortOrder,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt,
    remark = this.remark
)

