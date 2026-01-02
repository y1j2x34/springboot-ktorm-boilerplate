tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    // Spring Security for exception handling in GlobalExceptionHandler
    api("org.springframework.boot:spring-boot-starter-security")
    // Validation API for ConstraintViolationException handling
    api("org.springframework.boot:spring-boot-starter-validation")
    // JWT library for JWT exception handling
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
}

tasks.test {
    useJUnitPlatform()
}