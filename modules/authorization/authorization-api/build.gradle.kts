plugins {
    kotlin("jvm")
}

// Disable bootJar for library module
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    // Common module for shared utilities
    implementation(project(":infrastructure:infrastructure-common"))
}
