tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation("com.github.anji-plus:captcha:1.2.6")
    implementation(project(":infrastructure:infrastructure-core"))
    implementation(project(":infrastructure:infrastructure-common"))

    // Swagger/OpenAPI documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
