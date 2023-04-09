package com.vgerbot.ai.user.dao

import com.vgerbot.ai.foundation.dao.BaseDao
import com.vgerbot.ai.user.model.User
import com.vgerbot.ai.user.model.Users
import org.springframework.stereotype.Component

@Component
class UserDao: BaseDao<User, Users>(Users) {
}