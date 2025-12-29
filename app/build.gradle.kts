dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    implementation(project(":jwt-auth"))
    implementation(project(":rbac"))
    implementation(project(":tenant"))
    
    // Spring Security is needed because AppSecurityConfiguration uses it directly
    implementation("org.springframework.boot:spring-boot-starter-security")
    
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