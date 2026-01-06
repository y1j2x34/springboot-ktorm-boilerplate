dependencies {
    implementation(project(":modules:captcha"))
    implementation(project(":infrastructure:infrastructure-common"))
    implementation(project(":modules:auth:auth-jwt"))
    implementation(project(":modules:authorization:authorization-core"))
    implementation(project(":modules:tenant:tenant-core"))
    implementation(project(":modules:user:user-core"))
    implementation(project(":modules:auth:auth-wechat"))
    implementation(project(":modules:auth:auth-oauth"))
    implementation(project(":modules:dict:dict-core"))
    implementation(project(":modules:postgrest-query:postgrest-query-core"))

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
