tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-core"))
    implementation(project(":infrastructure:infrastructure-common"))
    
    // Ktorm for entity definitions
    implementation("org.ktorm:ktorm-core")
    
    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Jakarta Validation
    implementation("jakarta.validation:jakarta.validation-api")
}

