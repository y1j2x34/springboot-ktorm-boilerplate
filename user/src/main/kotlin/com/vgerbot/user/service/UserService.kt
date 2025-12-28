package com.vgerbot.user.service

import com.vgerbot.common.user.CreateUserDto
import com.vgerbot.common.user.UserDto
import com.vgerbot.common.user.UserService
import com.vgerbot.user.dao.UserDao
import com.vgerbot.user.model.User
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserServiceImpl : UserService {
    @Autowired
    lateinit var userDao: UserDao;

    @Autowired
    lateinit var encoder: PasswordEncoder;

    @Transactional
    override fun createUser(userDto: CreateUserDto): Boolean {
        val user = User();
        user.username = userDto.username;
        user.password = encoder.encode(userDto.password);
        user.email = userDto.email;
        val insertRows = userDao.add(
            user
        )
        return insertRows == 1;
    }

    override fun findUser(username: String): UserDto? = userDao.findOne { it.username.eq(username) }.let {
        if (it != null) UserDto(
            it.id,
            it.username,
            it.email,
            it.createdAt
        ) else null;
    }


}