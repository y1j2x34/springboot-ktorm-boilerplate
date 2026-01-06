package com.vgerbot.common.event

import org.springframework.context.ApplicationEvent

/**
 * 用户创建事件
 * 
 * 当新用户被创建时发布此事件，允许其他模块响应用户创建操作而不需要直接耦合
 * 
 * @property userId 新创建的用户ID
 * @property username 用户名
 * @property email 用户邮箱
 */
class UserCreatedEvent(
    source: Any,
    val userId: Int,
    val username: String,
    val email: String
) : ApplicationEvent(source) {
    override fun toString(): String {
        return "UserCreatedEvent(userId=$userId, username='$username', email='$email')"
    }
}

/**
 * 用户更新事件
 * 
 * 当用户信息被更新时发布此事件
 * 
 * @property userId 被更新的用户ID
 * @property username 用户名
 */
class UserUpdatedEvent(
    source: Any,
    val userId: Int,
    val username: String
) : ApplicationEvent(source)

/**
 * 用户删除事件
 * 
 * 当用户被删除时发布此事件
 * 
 * @property userId 被删除的用户ID
 * @property username 用户名
 */
class UserDeletedEvent(
    source: Any,
    val userId: Int,
    val username: String
) : ApplicationEvent(source)

