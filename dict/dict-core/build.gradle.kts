// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":core"))
    api(project(":dict:dict-api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    
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

