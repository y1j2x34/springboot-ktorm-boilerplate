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
    api(project(":modules:system:system-api"))
    
    // Core module for database entities and common utilities
    implementation(project(":infrastructure:infrastructure-core"))
    
    // Common module
    implementation(project(":infrastructure:infrastructure-common"))
    
    // Auth API module for AuthenticatedUserDetails
    implementation(project(":modules:auth:auth-common"))
    implementation(project(":modules:auth:auth-jwt"))
    
    // Authorization module
    implementation(project(":modules:authorization:authorization-api"))
    implementation(project(":modules:authorization:authorization-core"))
    
    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Spring Web for controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Database
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-jackson:3.6.0")
    implementation("org.ktorm:ktorm-support-mysql:3.6.0")
}
