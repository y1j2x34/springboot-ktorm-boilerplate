package com.vgerbot.user.service

import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.common.exception.ValidationException
import com.vgerbot.common.event.UserCreatedEvent
import com.vgerbot.user.dto.BindExternalIdentityDto
import com.vgerbot.user.dto.CreateExternalUserDto
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.dto.UserInfoDto
import com.vgerbot.user.dao.UserDao
import com.vgerbot.user.entity.User
import org.ktorm.dsl.eq
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userDao: UserDao,
    private val encoder: PasswordEncoder,
    private val eventPublisher: ApplicationEventPublisher
) : UserService {

    @Transactional
    override fun createUser(userDto: CreateUserDto): Boolean {
        if (userDto.password.isBlank()) {
            throw ValidationException("本地账号必须提供密码", "password")
        }

        ensureUsernameAvailable(userDto.username)

        val user = User {
            username = userDto.username
            password = encoder.encode(userDto.password)
            email = userDto.email
            phoneNumber = userDto.phoneNumber
            authProvider = null
            externalId = null
            isDeleted = false
        }

        return persistUser(user)
    }

    @Transactional
    override fun createExternalUser(userDto: CreateExternalUserDto): UserInfoDto? {
        validateExternalIdentity(userDto.authProvider, userDto.externalId)

        findUserByExternalIdentity(userDto.authProvider, userDto.externalId)?.let { return it }
        ensureUsernameAvailable(userDto.username)

        val user = User {
            username = userDto.username
            password = null
            email = userDto.email
            phoneNumber = userDto.phoneNumber
            authProvider = userDto.authProvider
            externalId = userDto.externalId
            isDeleted = false
        }

        persistUser(user)

        return user.toDto()
    }

    override fun findUser(username: String): UserInfoDto? = userDao.findByUsername(username)?.toDto()

    override fun findUserByExternalIdentity(authProvider: String, externalId: String): UserInfoDto? {
        validateExternalIdentity(authProvider, externalId)
        return userDao.findByExternalIdentity(authProvider, externalId)?.toDto()
    }

    @Transactional
    override fun bindExternalIdentity(userId: Int, binding: BindExternalIdentityDto): Boolean {
        validateExternalIdentity(binding.authProvider, binding.externalId)

        val existingUser = userDao.findByExternalIdentity(binding.authProvider, binding.externalId)
        if (existingUser != null && existingUser.id != userId) {
            throw ConflictException("外部身份已绑定到其他用户")
        }

        val user = userDao.findOneActive { it.id eq userId }
            ?: throw NotFoundException("用户不存在")

        if (
            user.externalId != null &&
            user.authProvider != null &&
            (user.externalId != binding.externalId || user.authProvider != binding.authProvider)
        ) {
            throw ConflictException("当前用户已绑定其他外部身份")
        }

        user.authProvider = binding.authProvider
        user.externalId = binding.externalId

        return userDao.update(user) == 1
    }

    private fun ensureUsernameAvailable(username: String) {
        if (userDao.findByUsername(username) != null) {
            throw ConflictException("用户名已存在")
        }
    }

    private fun validateExternalIdentity(authProvider: String, externalId: String) {
        if (authProvider.isBlank()) {
            throw ValidationException("外部认证提供方不能为空", "authProvider")
        }
        if (externalId.isBlank()) {
            throw ValidationException("外部身份ID不能为空", "externalId")
        }
    }

    private fun persistUser(user: User): Boolean {
        val insertRows = userDao.add(user)
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
        return insertRows == 1
    }

    private fun User.toDto(): UserInfoDto {
        return UserInfoDto(
            id = id,
            username = username,
            password = password,
            email = email,
            phoneNumber = phoneNumber,
            authProvider = authProvider,
            externalId = externalId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}