tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":core"))
    
    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

