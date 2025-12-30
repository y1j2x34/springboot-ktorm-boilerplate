// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":user:user-api"))
    implementation(project(":tenant:tenant-api"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}