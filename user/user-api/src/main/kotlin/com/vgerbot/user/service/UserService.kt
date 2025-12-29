package com.vgerbot.user.service

import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.dto.UserInfoDto


interface UserService {
    fun createUser(userDto: CreateUserDto): Boolean;
    fun findUser(username: String): UserInfoDto?
}
