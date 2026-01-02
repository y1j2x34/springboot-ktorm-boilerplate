package com.vgerbot.authorization.annotation

/**
 * 权限检查注解
 * 用于标注需要权限检查的方法或类
 * 
 * 使用示例：
 * ```
 * @RequiresPermission(resource = "user", action = "read")
 * fun getUser(id: Int): User
 * 
 * @RequiresPermission(resource = "user", action = "create", checkTenant = true)
 * fun createUser(user: User): User
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(
    /**
     * 资源名称
     */
    val resource: String,
    
    /**
     * 操作名称
     */
    val action: String,
    
    /**
     * 是否检查租户
     * 如果为 true，将使用当前用户的租户ID作为 domain 参数
     */
    val checkTenant: Boolean = false,
    
    /**
     * 错误消息
     */
    val message: String = "Permission denied"
)

