dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    implementation(project(":jwt-auth"))
    implementation(project(":authorization:authorization-core"))
    implementation(project(":tenant:tenant-core"))
    implementation(project(":user:user-core"))
    implementation(project(":wechat-auth"))
    implementation(project(":oauth-auth"))

    // Spring Security is needed because AppSecurityConfiguration uses it directly
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("me.paulschwarz:springboot3-dotenv:5.0.1")
    
    // Swagger/OpenAPI documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    runtimeOnly("com.github.anji-plus:captcha")
    implementation("io.projectreactor:reactor-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}