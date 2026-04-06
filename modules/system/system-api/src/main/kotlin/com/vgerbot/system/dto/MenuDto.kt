package com.vgerbot.system.dto

import java.time.LocalDateTime

data class MenuDto(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val path: String?,
    val component: String?,
    val permission: String?,
    val icon: String?,
    val sortOrder: Int,
    val type: Int,
    val visible: Boolean,
    val createdBy: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedBy: String? = null,
    val updatedAt: LocalDateTime? = null
)

data class MenuTreeDto(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val path: String?,
    val component: String?,
    val permission: String?,
    val icon: String?,
    val sortOrder: Int,
    val type: Int,
    val visible: Boolean,
    var children: List<MenuTreeDto> = emptyList()
)
