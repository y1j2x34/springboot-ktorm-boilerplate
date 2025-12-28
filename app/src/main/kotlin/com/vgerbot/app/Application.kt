package com.vgerbot.app;

import com.vgerbot.auth.JwtConfiguration
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.get
import java.util.*


@SpringBootApplication(exclude = [JwtConfiguration::class])
@ComponentScan("com.vgerbot.*")
class Application {
   @Bean
   fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner? {
      return CommandLineRunner { args: Array<String?>? ->
         println("Let's inspect the beans provided by Spring Boot:")
         val beanNames = ctx.beanDefinitionNames
         Arrays.sort(beanNames)
         for (beanName in beanNames) {
            println(beanName)
         }
      }
   }
}

fun main(args: Array<String>) {
   var ctx = runApplication<Application>()
   println(ctx.environment.get("jwt.secret"));
}