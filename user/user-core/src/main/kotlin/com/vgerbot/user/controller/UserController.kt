package com.vgerbot.user.controller

import com.vgerbot.user.dao.UserDao
import com.vgerbot.user.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * User Controller
 * Provides REST API for user management
 */
@Tag(name = "User", description = "User management APIs")
@RestController
@RequestMapping("com/vgerbot/user")
class UserController {
    @Autowired
    lateinit var userDao: UserDao;

    /**
     * Get user list
     */
    @Operation(summary = "Get user list", description = "Retrieve a list of all users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @GetMapping("list")
    fun getUserList(): List<User> =
        userDao.findAll()
}