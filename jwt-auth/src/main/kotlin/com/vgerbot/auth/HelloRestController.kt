package com.vgerbot.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Hello Controller
 * Provides simple hello endpoints for testing authentication
 */
@Tag(name = "Hello", description = "Hello world test APIs")
@RestController
@RequestMapping("hello")
class HelloRestController {
    @Operation(summary = "Hello user", description = "Hello endpoint for authenticated users")
    @ApiResponse(responseCode = "200", description = "Hello message returned")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("user")
    fun helloUser() = ResponseEntity.ok("Hello User")

    @Operation(summary = "Hello admin", description = "Hello endpoint for admin users")
    @ApiResponse(responseCode = "200", description = "Hello message returned")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("admin")
    fun helloAdmin() = ResponseEntity.ok("Hello Admin")

}
