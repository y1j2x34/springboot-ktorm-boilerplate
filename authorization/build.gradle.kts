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
    // jCasbin - 使用最新稳定版本
    implementation("org.casbin:jcasbin:1.78.0")
    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

