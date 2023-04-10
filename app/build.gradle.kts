
group = "com.vgerbot"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":captcha"))
    runtimeOnly("com.github.anji-plus:captcha")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}