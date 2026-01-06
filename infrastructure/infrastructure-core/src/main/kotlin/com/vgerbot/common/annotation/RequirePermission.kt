package com.vgerbot.common.annotation

/**
 * 权限注解 - 用于标记需要权限控制的方法
 * 
 * 使用示例:
 * ```kotlin
 * @RequirePermission(resource = "user", action = "delete")
 * fun deleteUser(@PathVariable id: Int): ResponseEntity<*> {
 *     // 方法实现
 * }
 * ```
 * 
 * @property resource 资源类型（如 user, order, product）
 * @property action 操作类型（如 read, create, update, delete）
 * @property code 权限代码（如果不为空，则优先使用此代码，格式通常为 resource:action）
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequirePermission(
    val resource: String = "",
    val action: String = "",
    val code: String = ""
)

