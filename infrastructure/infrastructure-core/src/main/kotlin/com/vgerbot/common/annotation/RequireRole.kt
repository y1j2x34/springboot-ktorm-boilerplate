package com.vgerbot.common.annotation

/**
 * 需要角色注解 - 用于标记需要特定角色的方法
 * 
 * 使用示例:
 * ```kotlin
 * @RequireRole(codes = ["ROLE_ADMIN"])
 * fun adminOnlyOperation(): ResponseEntity<*> {
 *     // 只有管理员可以访问
 * }
 * ```
 * 
 * @property codes 角色代码数组（用户需要拥有其中任意一个角色）
 * @property requireAll 是否需要拥有所有指定的角色（默认 false，即拥有任意一个即可）
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequireRole(
    val codes: Array<String>,
    val requireAll: Boolean = false
)

