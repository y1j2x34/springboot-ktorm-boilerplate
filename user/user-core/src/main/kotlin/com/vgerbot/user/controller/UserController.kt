package com.vgerbot.user.controller

import com.vgerbot.user.dao.UserDao
import com.vgerbot.user.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("com/vgerbot/user")
class UserController {
    @Autowired
    lateinit var userDao: UserDao;

    @GetMapping("list")
    fun getUserList(): List<User> =
        userDao.findAll()
}