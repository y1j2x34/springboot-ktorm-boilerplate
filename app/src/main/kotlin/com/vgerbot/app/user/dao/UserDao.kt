package com.vgerbot.app.user.dao

import com.vgerbot.app.foundation.dao.BaseDao
import com.vgerbot.app.user.model.User
import com.vgerbot.app.user.model.Users
import org.springframework.stereotype.Component

@Component
class UserDao: BaseDao<User, Users>(Users) {
}