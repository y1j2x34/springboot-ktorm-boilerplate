package com.vgerbot.system.controller

import com.vgerbot.auth.common.principal.AuthenticatedUserDetails
import com.vgerbot.common.controller.created
import com.vgerbot.common.controller.noContent
import com.vgerbot.common.controller.ok
import com.vgerbot.system.dto.MenuDto
import com.vgerbot.system.service.MenuService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/menus")
@Tag(name = "Menu", description = "系统菜单管理")
class MenuController(
    private val menuService: MenuService
) {

    @Operation(summary = "获取当前用户的路由菜单")
    @GetMapping("/routes")
    fun getUserRoutes(
        @AuthenticationPrincipal principal: AuthenticatedUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val userId = principal.userId
        val menuTree = menuService.getCurrentUserMenuTree(userId)
        return menuTree.ok()
    }

    @Operation(summary = "获取所有菜单树", description = "系统管理员用于配置菜单，需要 admin 权限")
    @PreAuthorize("hasRole('admin') or hasRole('super_admin')")
    @GetMapping
    fun getAllMenus(): ResponseEntity<Map<String, Any>> {
        val menuTree = menuService.getAllMenuTree()
        return menuTree.ok()
    }

    @Operation(summary = "获取菜单详情")
    @PreAuthorize("hasRole('admin') or hasRole('super_admin')")
    @GetMapping("/{id}")
    fun getMenuById(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val menu = menuService.getMenuById(id)
        return if (menu != null) {
            menu.ok()
        } else {
            com.vgerbot.common.controller.notFound("菜单不存在")
        }
    }

    @Operation(summary = "创建菜单")
    @PreAuthorize("hasRole('admin') or hasRole('super_admin')")
    @PostMapping
    fun createMenu(@RequestBody dto: MenuDto): ResponseEntity<Map<String, Any>> {
        val createdMenu = menuService.createMenu(dto)
        return createdMenu.created("创建成功")
    }

    @Operation(summary = "更新菜单")
    @PreAuthorize("hasRole('admin') or hasRole('super_admin')")
    @PutMapping("/{id}")
    fun updateMenu(
        @PathVariable id: Long,
        @RequestBody dto: MenuDto
    ): ResponseEntity<Map<String, Any>> {
        val updatedMenu = menuService.updateMenu(id, dto)
        return updatedMenu.ok("更新成功")
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("hasRole('admin') or hasRole('super_admin')")
    @DeleteMapping("/{id}")
    fun deleteMenu(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        menuService.deleteMenu(id)
        return noContent()
    }
}
