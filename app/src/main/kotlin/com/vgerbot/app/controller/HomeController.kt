package com.vgerbot.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller()
@RequestMapping("/home")
class HomeController {
    @GetMapping("index.html")
    fun index(): ResponseEntity<String> {
        return ResponseEntity.ok("Hello World!")
    }
}