dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    implementation(project(":jwt-auth"))
    implementation(project(":rbac:rbac-core"))
    implementation(project(":tenant:tenant-core"))
    implementation(project(":user:user-core"))

    // Spring Security is needed because AppSecurityConfiguration uses it directly
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("me.paulschwarz:springboot3-dotenv:5.0.1")
    
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