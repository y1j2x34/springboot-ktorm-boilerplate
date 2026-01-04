// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":user:user-api"))
    implementation(project(":jwt-auth"))
    implementation(project(":oauth-auth"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // WxJava - 微信开发 SDK
    implementation("com.github.binarywang:weixin-java-mp:4.6.0")       // 微信公众号
    implementation("com.github.binarywang:weixin-java-open:4.6.0")     // 微信开放平台

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

