package com.vgerbot.user.dao

import com.vgerbot.common.dao.BaseDao
import com.vgerbot.user.model.User
import com.vgerbot.user.model.Users
import org.springframework.stereotype.Component

@Component
class UserDao: BaseDao<User, Users>(Users) {
}