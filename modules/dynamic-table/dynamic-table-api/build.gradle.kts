// API module for dynamic-table
// Contains interfaces and DTOs

tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    // Ktorm for BaseTable type
    api("org.ktorm:ktorm-core")
    
    // Validation annotations
    api("org.springframework.boot:spring-boot-starter-validation")
}

kotlin {
    jvmToolchain(17)
}

