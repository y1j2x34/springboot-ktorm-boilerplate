tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation("com.github.anji-plus:captcha:1.2.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}