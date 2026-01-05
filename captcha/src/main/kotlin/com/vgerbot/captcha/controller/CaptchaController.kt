package com.vgerbot.captcha.controller

import com.anji.captcha.model.common.ResponseModel
import com.anji.captcha.model.vo.CaptchaVO
import com.anji.captcha.service.CaptchaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Captcha Controller
 * Provides REST API for captcha generation and verification
 */
@Tag(name = "Captcha", description = "Captcha generation and verification APIs")
@RestController("ajCaptchaController")
@RequestMapping("captcha")
class CaptchaController {
    @Autowired
    lateinit var captchaService: CaptchaService;

    @Operation(summary = "Get captcha identity", description = "Get captcha identity value")
    @ApiResponse(responseCode = "200", description = "Identity returned")
    @GetMapping("identity")
    fun identity(
        @Parameter(description = "Identity value", required = true)
        @RequestParam value: String
    ): String {
        return value;
    }

    @Operation(summary = "Get captcha", description = "Generate a new captcha")
    @ApiResponse(responseCode = "200", description = "Captcha generated successfully")
    @PostMapping("/get")
    operator fun get(
        @Parameter(description = "Captcha request data")
        @RequestBody captchaVO: CaptchaVO?
    ): ResponseModel? {
        return captchaService[captchaVO]
    }

    @Operation(summary = "Check captcha", description = "Check captcha verification")
    @ApiResponse(responseCode = "200", description = "Captcha check completed")
    @PostMapping("/check")
    fun check(
        @Parameter(description = "Captcha verification data")
        @RequestBody captchaVO: CaptchaVO?
    ): ResponseModel? {
        return captchaService.check(captchaVO)
    }

    @Operation(summary = "Verify captcha", description = "Verify captcha answer")
    @ApiResponse(responseCode = "200", description = "Captcha verification completed")
    @PostMapping("/verify")
    fun verify(
        @Parameter(description = "Captcha verification data")
        @RequestBody captchaVO: CaptchaVO?
    ): ResponseModel? {
        return captchaService.verification(captchaVO)
    }
}