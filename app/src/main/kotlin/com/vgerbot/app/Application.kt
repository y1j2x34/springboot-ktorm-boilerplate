package com.vgerbot.app;

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.vgerbot.*")
class Application

fun main(args: Array<String>) {
   runApplication<Application>()
}