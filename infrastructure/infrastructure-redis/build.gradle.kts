tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    // Spring Data Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
    // Redis connection pool
    implementation("org.apache.commons:commons-pool2")
    // Jackson for JSON serialization
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Kotlin coroutines support for Redis
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.test {
    useJUnitPlatform()
}

