// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-core"))
    api(project(":modules:scheduler:scheduler-api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Spring Scheduling for cron jobs
    implementation("org.springframework:spring-context")
    
    // Swagger/OpenAPI documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // SLF4J for logging
    implementation("org.slf4j:slf4j-api")
    
    // JavaScript engine for JavaScript runner
    // Using GraalVM Polyglot API (more flexible)
    // Note: GraalVM dependencies may require additional Maven repository configuration
    // For now, we'll use a simpler approach with ScriptEngineManager
    // If GraalVM is available, it will be used; otherwise, fallback to other engines
    
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

