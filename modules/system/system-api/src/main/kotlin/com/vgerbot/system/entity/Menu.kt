package com.vgerbot.system.entity

import com.vgerbot.common.entity.AuditableEntity
import org.ktorm.entity.Entity

interface Menu : AuditableEntity<Menu> {
    companion object : Entity.Factory<Menu>()
    
    val id: Long
    var parentId: Long?
    var name: String
    var path: String?
    var component: String?
    var permission: String?
    var icon: String?
    var sortOrder: Int
    var type: Int
    var visible: Boolean
}
