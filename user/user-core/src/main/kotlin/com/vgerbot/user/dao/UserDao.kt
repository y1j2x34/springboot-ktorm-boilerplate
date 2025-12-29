package com.vgerbot.user.dao

import com.vgerbot.common.dao.AbstractBaseDao
import com.vgerbot.user.model.User
import com.vgerbot.user.model.Users
import org.springframework.stereotype.Repository

@Repository
class UserDaoImpl: AbstractBaseDao<User, Users>(Users), UserDao {
}