package com.vgerbot.common.annotation

/**
 * 公开访问注解 - 标记不需要权限检查的方法
 * 
 * 使用示例:
 * ```kotlin
 * @PublicAccess
 * @GetMapping("/public/info")
 * fun getPublicInfo(): ResponseEntity<*> {
 *     // 任何人都可以访问
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PublicAccess

