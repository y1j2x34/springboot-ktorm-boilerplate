plugins {
    kotlin("jvm") version "1.9.23"
}

// Disable bootJar for parent module
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

group = "com.vgerbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}