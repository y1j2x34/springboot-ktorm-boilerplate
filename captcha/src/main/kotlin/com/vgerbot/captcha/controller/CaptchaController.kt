package com.vgerbot.captcha.controller

import com.anji.captcha.model.common.ResponseModel
import com.anji.captcha.model.vo.CaptchaVO
import com.anji.captcha.service.CaptchaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("ajCaptchaController")
@RequestMapping("captcha")
class CaptchaController {
    @Autowired
    lateinit var captchaService: CaptchaService;

    @GetMapping("identity")
    fun identity(@RequestParam value: String): String {
        return value;
    }

    @PostMapping("/get")
    operator fun get(@RequestBody captchaVO: CaptchaVO?): ResponseModel? {
        return captchaService[captchaVO]
    }

    @PostMapping("/check")
    fun check(@RequestBody captchaVO: CaptchaVO?): ResponseModel? {
        return captchaService.check(captchaVO)
    }

    @PostMapping("/verify")
    fun verify(@RequestBody captchaVO: CaptchaVO?): ResponseModel? {
        return captchaService.verification(captchaVO)
    }
}