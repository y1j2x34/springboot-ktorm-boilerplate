import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.8.20" // The version of Kotlin to use
    kotlin("plugin.spring") version "1.8.20"
}

group = "com.vgerbot"
version = "1.0-SNAPSHOT"

java {
//    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
//    sourceCompatibility = JavaVersion.VERSION_17
}

allprojects {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/grails-core")
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        mavenCentral()
    }
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // Jackson extensions for Kotlin for working with JSON
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.20") // Kotlin reflection library, required for working with Spring

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Kotlin standard library

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-jackson:3.6.0")
    implementation("org.ktorm:ktorm-support-mysql:3.6.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.3")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.withType<KotlinCompile> { // Settings for `KotlinCompile` tasks
    kotlinOptions { // Kotlin compiler options
        freeCompilerArgs = listOf("-Xjsr305=strict") // `-Xjsr305=strict` enables the strict mode for JSR-305 annotations
        jvmTarget = "17" // This option specifies the target version of the generated JVM bytecode
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}