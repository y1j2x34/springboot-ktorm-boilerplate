dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    implementation(project(":jwt-auth"))
    runtimeOnly("com.github.anji-plus:captcha")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}