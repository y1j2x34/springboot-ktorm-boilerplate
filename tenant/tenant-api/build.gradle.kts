
group = "com.vgerbot"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}