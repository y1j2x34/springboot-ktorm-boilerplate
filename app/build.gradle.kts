dependencies {
    implementation(project(":captcha"))
    implementation(project(":common"))
    implementation(project(":jwt-auth"))
    implementation(project(":rbac"))
    implementation(project(":tenant"))
    runtimeOnly("com.github.anji-plus:captcha")
    implementation("io.projectreactor:reactor-core:3.2.8.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}