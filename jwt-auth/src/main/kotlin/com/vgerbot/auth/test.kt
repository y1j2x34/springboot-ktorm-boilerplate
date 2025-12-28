package com.vgerbot.auth

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

fun main(args: Array<String>) {
    val md5 = "e10adc3949ba59abbe56e057f20f883e"
    val encoder = BCryptPasswordEncoder();
    println(encoder.encode(md5));

    println(encoder.matches(md5, "\$2a\$10\$tWXtlnxX4oZE5oWjLsZ/Aegwl9rgc/b5nVzS1zeXjufAGm2L0KAlq"))
}