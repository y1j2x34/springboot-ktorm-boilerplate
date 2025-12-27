
dependencies {
    implementation(project(":common"))
    implementation(project(":user"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}