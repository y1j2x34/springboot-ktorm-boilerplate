dependencies {
    implementation(project(":common"))
    implementation("org.springframework.security:spring-security-crypto:5.7.2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}