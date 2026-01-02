package com.vgerbot.authorization.annotation

/**
 * 角色检查注解
 * 用于标注需要特定角色的方法或类
 * 
 * 使用示例：
 * ```
 * @RequiresRole(roles = ["ROLE_ADMIN"])
 * fun deleteUser(id: Int)
 * 
 * @RequiresRole(roles = ["ROLE_ADMIN", "ROLE_MANAGER"], requireAll = false)
 * fun approveOrder(id: Int)
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresRole(
    /**
     * 角色列表
     */
    val roles: Array<String>,
    
    /**
     * 是否需要所有角色
     * true: 用户必须拥有所有指定角色
     * false: 用户拥有任意一个角色即可
     */
    val requireAll: Boolean = false,
    
    /**
     * 是否检查租户
     */
    val checkTenant: Boolean = false,
    
    /**
     * 错误消息
     */
    val message: String = "Role required"
)

