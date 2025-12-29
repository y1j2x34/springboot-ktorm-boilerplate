package com.vgerbot.common.annotation

/**
 * 权限注解
 * 
 * 用于标记 Controller 方法需要的权限
 * 
 * 使用示例:
 * ```
 * @RequiresPermission(resource = "user", action = "delete")
 * @DeleteMapping("/{id}")
 * fun deleteUser(@PathVariable id: Int): ResponseEntity<*> {
 *     // 只有拥有 user:delete 权限的用户才能访问
 * }
 * ```
 * 
 * 如果不指定 resource 和 action，可以直接指定完整的权限码:
 * ```
 * @RequiresPermission(value = "user:delete")
 * ```
 * 
 * @param value 完整的权限码，如 "user:delete"。如果指定了此参数，resource 和 action 将被忽略
 * @param resource 资源类型，如 "user", "order"
 * @param action 操作类型，如 "read", "create", "update", "delete"
 * @param requireAll 当指定多个权限时，是否要求拥有所有权限（默认 false，表示只需要其中之一）
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequiresPermission(
    val value: String = "",
    val resource: String = "",
    val action: String = "",
    val requireAll: Boolean = false
)

/**
 * 多权限注解
 * 
 * 用于标记需要多个权限的情况
 * 
 * 使用示例:
 * ```
 * @RequiresPermissions(
 *     RequiresPermission(resource = "user", action = "read"),
 *     RequiresPermission(resource = "user", action = "update"),
 *     requireAll = true  // 需要同时拥有两个权限
 * )
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequiresPermissions(
    vararg val value: RequiresPermission,
    val requireAll: Boolean = false
)

/**
 * 角色注解
 * 
 * 用于标记 Controller 方法需要的角色
 * 
 * 使用示例:
 * ```
 * @RequiresRole("ROLE_ADMIN")
 * @GetMapping("/admin/dashboard")
 * fun adminDashboard(): ResponseEntity<*> {
 *     // 只有管理员才能访问
 * }
 * ```
 * 
 * @param value 角色代码，如 "ROLE_ADMIN", "ROLE_USER"
 * @param requireAll 当指定多个角色时，是否要求拥有所有角色（默认 false，表示只需要其中之一）
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequiresRole(
    vararg val value: String,
    val requireAll: Boolean = false
)
