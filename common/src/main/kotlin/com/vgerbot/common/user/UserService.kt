package com.vgerbot.common.user


interface UserService {
    fun createUser(userDto: CreateUserDto): Boolean;
    fun findUser(username: String): UserDto?
}
