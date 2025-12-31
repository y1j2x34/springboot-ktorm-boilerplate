// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

group = "com.vgerbot"
version = "1.0-SNAPSHOT"
