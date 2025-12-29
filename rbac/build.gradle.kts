plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.ktorm:ktorm-core")
    implementation("org.ktorm:ktorm-support-mysql")
}
