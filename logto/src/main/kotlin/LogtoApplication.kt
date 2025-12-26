package com.vgerbot.logto

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan(basePackages = ["com.vgerbot.logto"])
class LogtoApplication {
}
fun main(args: Array<String>) {
    runApplication<LogtoApplication>(*args) {
//        setAdditionalProfiles("dev")
    }
}
