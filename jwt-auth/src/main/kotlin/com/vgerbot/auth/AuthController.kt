package com.vgerbot.auth

import com.vgerbot.auth.data.AuthRequest
import com.vgerbot.common.user.CreateUserDto
import com.vgerbot.common.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("public")
class AuthController {
    @Autowired
    lateinit var authenticationManager: AuthenticationManager
    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService
    @Autowired
    lateinit var jwtTokenUtils: JwtTokenUtils
    @Autowired
    lateinit var userService: UserService;
    @PostMapping("login")
    fun login(@RequestBody req: AuthRequest): ResponseEntity<String> {

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.username, req.password)
        )
        val userDetails = userDetailsService.loadUserByUsername(req.username) ?: throw UsernameNotFoundException("User not exists: ${req.username}");
        val token = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).build();
    }
    @PutMapping("register")
    fun createUser(@RequestBody userDto: CreateUserDto): ResponseEntity<Boolean> {
        val success = userService.createUser(userDto);
        return if(success) {
            ResponseEntity.ok().body(true);
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(false)
        }
    }
}