package com.vgerbot.app.controller

import com.vgerbot.oauth.service.CustomOidcUser
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller()
@RequestMapping("/home")
class HomeController {
    @GetMapping("index.html")
    fun index(a: Authentication): ResponseEntity<String> {
        return ResponseEntity.ok("Hello: ${(a.principal as CustomOidcUser).email}")
    }
}