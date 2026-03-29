// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-core"))
    implementation(project(":modules:user:user-api"))
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
