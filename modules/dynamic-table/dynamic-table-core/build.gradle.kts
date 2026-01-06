// Core implementation module for dynamic-table
// Contains DynamicTable, DynamicTableManager, Controller, etc.

tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    api(project(":modules:dynamic-table:dynamic-table-api"))
    implementation(project(":infrastructure:infrastructure-core"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Ktorm ORM
    implementation("org.ktorm:ktorm-core")
    implementation("org.ktorm:ktorm-support-mysql")
    
    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // SLF4J for logging
    implementation("org.slf4j:slf4j-api")
    
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
