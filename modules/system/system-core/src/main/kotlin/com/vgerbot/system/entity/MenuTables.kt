package com.vgerbot.system.entity

import com.vgerbot.common.entity.AuditableTable
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Menus : AuditableTable<Menu>("sys_menu") {
    val id = long("id").primaryKey().bindTo { it.id }
    val parentId = long("parent_id").bindTo { it.parentId }
    val name = varchar("name").bindTo { it.name }
    val path = varchar("path").bindTo { it.path }
    val component = varchar("component").bindTo { it.component }
    val permission = varchar("permission").bindTo { it.permission }
    val icon = varchar("icon").bindTo { it.icon }
    val sortOrder = int("sort_order").bindTo { it.sortOrder }
    val type = int("type").bindTo { it.type }
    val visible = boolean("visible").bindTo { it.visible }
}

val Database.menus get() = this.sequenceOf(Menus)
