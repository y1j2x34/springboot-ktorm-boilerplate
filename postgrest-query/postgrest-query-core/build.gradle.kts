// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":core"))
    api(project(":postgrest-query:postgrest-query-api"))
    implementation(project(":authorization:authorization-api"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    
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

