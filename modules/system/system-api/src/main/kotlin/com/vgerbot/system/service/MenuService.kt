package com.vgerbot.system.service

import com.vgerbot.system.dto.MenuDto
import com.vgerbot.system.dto.MenuTreeDto

interface MenuService {
    
    /**
     * 获取当前用户的路由菜单树
     * @param userId 当前用户ID
     * @return 树形结构的菜单列表，不包含按钮类型(type=2)
     */
    fun getCurrentUserMenuTree(userId: Int): List<MenuTreeDto>
    
    /**
     * 获取所有菜单树(管理端使用)
     * @return 完整的系统菜单树
     */
    fun getAllMenuTree(): List<MenuTreeDto>
    
    /**
     * 根据ID获取菜单详情
     */
    fun getMenuById(id: Long): MenuDto?
    
    /**
     * 创建菜单
     */
    fun createMenu(dto: MenuDto): MenuDto
    
    /**
     * 更新菜单
     */
    fun updateMenu(id: Long, dto: MenuDto): MenuDto
    
    /**
     * 删除菜单及其子菜单
     */
    fun deleteMenu(id: Long)
}
