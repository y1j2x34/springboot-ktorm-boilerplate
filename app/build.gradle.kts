dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    runtimeOnly("com.github.anji-plus:captcha")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}