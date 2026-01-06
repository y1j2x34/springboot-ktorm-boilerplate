// Disable bootJar for library modules
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}

dependencies {
    implementation(project(":infrastructure:infrastructure-common"))
    implementation(project(":infrastructure:infrastructure-core"))
    implementation(project(":modules:user:user-api"))
    implementation(project(":modules:auth:auth-jwt"))
    implementation(project(":modules:auth:auth-oauth"))
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
