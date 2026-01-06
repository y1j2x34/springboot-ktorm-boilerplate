plugins {
    kotlin("jvm")
}

// Disable bootJar for library module
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    // API module
    api(project(":modules:authorization:authorization-api"))
    
    // Core module for database entities and common utilities
    implementation(project(":infrastructure:infrastructure-core"))
    
    // Common module
    implementation(project(":infrastructure:infrastructure-common"))
    
    // Tenant API module (only for DTOs/interfaces, not entities)
    implementation(project(":modules:tenant:tenant-api"))
    
    // jCasbin - 使用最新稳定版本
    implementation("org.casbin:jcasbin:1.78.0")
    
    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Spring Web for controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Database
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-jackson:3.6.0")
    implementation("org.ktorm:ktorm-support-mysql:3.6.0")
}
