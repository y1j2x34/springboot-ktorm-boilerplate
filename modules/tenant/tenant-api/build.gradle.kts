tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

group = "com.vgerbot"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":infrastructure:infrastructure-common"))
    implementation(project(":infrastructure:infrastructure-core"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
