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
    // Core module for Entity and Table interfaces
    implementation(project(":infrastructure:infrastructure-core"))
    
    // Common module for shared utilities
    implementation(project(":infrastructure:infrastructure-common"))
}
