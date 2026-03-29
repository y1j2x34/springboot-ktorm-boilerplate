package com.vgerbot.user.service

import com.vgerbot.user.dto.BindExternalIdentityDto
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.dto.CreateExternalUserDto
import com.vgerbot.user.dto.UserInfoDto


interface UserService {
    fun createUser(userDto: CreateUserDto): Boolean;
    fun createExternalUser(userDto: CreateExternalUserDto): UserInfoDto?
    fun findUser(username: String): UserInfoDto?
    fun findUserByExternalIdentity(authProvider: String, externalId: String): UserInfoDto?
    fun bindExternalIdentity(userId: Int, binding: BindExternalIdentityDto): Boolean
}
