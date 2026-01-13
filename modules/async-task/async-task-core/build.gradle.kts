// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-core"))
    api(project(":modules:async-task:async-task-api"))
    implementation(project(":modules:scheduler:scheduler-api"))
    implementation(project(":modules:notification:notification-api"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Spring Scheduling for consumer polling
    implementation("org.springframework:spring-context")
    
    // Redis for distributed lock (optional)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // SLF4J for logging
    implementation("org.slf4j:slf4j-api")
    
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

