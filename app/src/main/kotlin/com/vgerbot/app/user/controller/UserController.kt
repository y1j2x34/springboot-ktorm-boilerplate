package com.vgerbot.app.user.controller

import com.vgerbot.app.user.dao.UserDao
import com.vgerbot.app.user.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @Autowired
    lateinit var userDao: UserDao;

    @GetMapping("/user/list")
    fun getUserList(): List<User> =
        userDao.findAll()
}