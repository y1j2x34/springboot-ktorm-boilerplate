package com.vgerbot.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("hello")
class HelloRestController {
    @GetMapping("user")
    fun helloUser() = ResponseEntity.ok("Hello User")

    @GetMapping("admin")
    fun helloAdmin() = ResponseEntity.ok("Hello Admin")

}
