package com.vgerbot.logto

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class IndexController {
    @GetMapping("/")
    fun index(principal: Principal?) = "index";

    @GetMapping("/sign-in")
    fun login() = "login"
}