// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-core"))
    api(project(":modules:postgrest-query:postgrest-query-api"))
    implementation(project(":modules:authorization:authorization-api"))
    api(project(":modules:dynamic-table:dynamic-table-core"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Ktorm ORM
    implementation("org.ktorm:ktorm-core")
    implementation("org.ktorm:ktorm-support-mysql")
    
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
