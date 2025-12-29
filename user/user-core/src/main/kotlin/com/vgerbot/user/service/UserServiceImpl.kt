package com.vgerbot.user.service

import com.vgerbot.common.event.UserCreatedEvent
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.dto.UserInfoDto
import com.vgerbot.user.dao.UserDao
import com.vgerbot.user.model.User
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserServiceImpl : UserService {
    @Autowired
    lateinit var userDao: UserDao;

    @Autowired
    lateinit var encoder: PasswordEncoder;

    @Autowired
    lateinit var eventPublisher: ApplicationEventPublisher;

    @Transactional
    override fun createUser(userDto: CreateUserDto): Boolean {
        val user = User();
        user.username = userDto.username;
        user.password = encoder.encode(userDto.password);
        user.email = userDto.email;
        user.phoneNumber = userDto.phoneNumber;
        val insertRows = userDao.add(
            user
        )
        
        // 发布用户创建事件，允许其他模块响应（如 tenant 模块进行租户绑定）
        if (insertRows == 1) {
            eventPublisher.publishEvent(
                UserCreatedEvent(
                    source = this,
                    userId = user.id,
                    username = user.username,
                    email = user.email
                )
            )
        }
        
        return insertRows == 1;
    }

    override fun findUser(username: String): UserInfoDto? = userDao.findOne { it.username.eq(username) }.let {
        if (it != null) UserInfoDto(
            it.id,
            it.username,
            it.email,
            it.phoneNumber,
            it.createdAt
        ) else null;
    }


}