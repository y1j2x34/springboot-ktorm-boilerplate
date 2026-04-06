package com.vgerbot.system.service

import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.system.dao.MenuDao
import com.vgerbot.system.dto.MenuDto
import com.vgerbot.system.dto.MenuTreeDto
import com.vgerbot.system.entity.Menu
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.notEq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class MenuServiceImpl(
    private val menuDao: MenuDao,
    private val authorizationService: AuthorizationService
) : MenuService {

    override fun getCurrentUserMenuTree(userId: Int): List<MenuTreeDto> {
        // 1. 如果是超级管理员，或者拥有 "admin" 角色，可以返回所有可用的目录和菜单
        val roles = authorizationService.getRolesForUser(userId)
        val isAdmin = roles.contains("admin") || roles.contains("super_admin")

        // 2. 查询所有可用的菜单项（排除按钮类型：type=2）
        val allMenus = menuDao.findListActive { it.type notEq 2 and (it.visible eq true) }
            .sortedBy { it.sortOrder }

        if (isAdmin) {
            return buildMenuTree(allMenus)
        }

        // 3. 如果不是管理员，需要从 Casbin 获取用户的全部权限 (通过 getUserPermissions 或者隐式计算)
        // 获取直接和角色继承获得的所有权限（resource和action，当前场景一般只需匹配 permission 字段）
        val userPermissions = authorizationService.getPermissionsForSubject(userId.toString()).map { it.resource }

        // 4. 根据权限过滤菜单
        val authorizedMenus = allMenus.filter { menu ->
            menu.permission.isNullOrBlank() || userPermissions.contains(menu.permission)
        }

        return buildMenuTree(authorizedMenus)
    }

    override fun getAllMenuTree(): List<MenuTreeDto> {
        val allMenus = menuDao.findAllActive().sortedBy { it.sortOrder }
        return buildMenuTree(allMenus)
    }

    override fun getMenuById(id: Long): MenuDto? {
        return menuDao.findOneActive { it.id eq id }?.toDto()
    }

    @Transactional
    override fun createMenu(dto: MenuDto): MenuDto {
        val menu = Menu {
            parentId = dto.parentId
            name = dto.name
            path = dto.path
            component = dto.component
            permission = dto.permission
            icon = dto.icon
            sortOrder = dto.sortOrder
            type = dto.type
            visible = dto.visible
        }
        menuDao.add(menu)
        return menu.toDto()
    }

    @Transactional
    override fun updateMenu(id: Long, dto: MenuDto): MenuDto {
        val menu = menuDao.findOneActive { it.id eq id }
            ?: throw NotFoundException("菜单不存在")

        menu.parentId = dto.parentId
        menu.name = dto.name
        menu.path = dto.path
        menu.component = dto.component
        menu.permission = dto.permission
        menu.icon = dto.icon
        menu.sortOrder = dto.sortOrder
        menu.type = dto.type
        menu.visible = dto.visible

        menuDao.update(menu)
        return menu.toDto()
    }

    @Transactional
    override fun deleteMenu(id: Long) {
        val menu = menuDao.findOneActive { it.id eq id }
            ?: throw NotFoundException("菜单不存在")

        // 检查是否有子菜单
        val hasChildren = menuDao.anyMatched { it.parentId eq id }
        if (hasChildren) {
            throw IllegalArgumentException("包含子菜单，无法删除")
        }

        menuDao.softDelete(id)
    }

    private fun buildMenuTree(menus: List<Menu>): List<MenuTreeDto> {
        val dtos = menus.map { it.toTreeDto() }
        val idMap = dtos.associateBy { it.id }

        val rootNodes = mutableListOf<MenuTreeDto>()

        for (dto in dtos) {
            val parentId = dto.parentId
            if (parentId == null || parentId == 0L || !idMap.containsKey(parentId)) {
                rootNodes.add(dto)
            } else {
                val parent = idMap[parentId]
                if (parent != null) {
                    val children = parent.children.toMutableList()
                    children.add(dto)
                    parent.children = children
                }
            }
        }
        return rootNodes
    }

    private fun Menu.toDto(): MenuDto {
        return MenuDto(
            id = this.id,
            parentId = this.parentId,
            name = this.name,
            path = this.path,
            component = this.component,
            permission = this.permission,
            icon = this.icon,
            sortOrder = this.sortOrder,
            type = this.type,
            visible = this.visible,
            createdBy = this.createdBy?.toString(),
            createdAt = null,
            updatedBy = this.updatedBy?.toString(),
            updatedAt = null
        )
    }

    private fun Menu.toTreeDto(): MenuTreeDto {
        return MenuTreeDto(
            id = this.id,
            parentId = this.parentId,
            name = this.name,
            path = this.path,
            component = this.component,
            permission = this.permission,
            icon = this.icon,
            sortOrder = this.sortOrder,
            type = this.type,
            visible = this.visible
        )
    }
}
