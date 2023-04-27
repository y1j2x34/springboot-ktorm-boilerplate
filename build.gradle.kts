import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import io.spring.gradle.dependencymanagement.dsl.ImportsHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.1" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.8.20" apply false
    kotlin("plugin.spring") version "1.8.20" apply false
}


subprojects {
    apply { plugin("org.jetbrains.kotlin.jvm") }
    apply { plugin("org.springframework.boot") }
    apply { plugin("io.spring.dependency-management") }
    apply { plugin("org.jetbrains.kotlin.plugin.spring") }

    dependencies {
        val implementation by configurations;
        val runtimeOnly by configurations;
        val testImplementation by configurations;

        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("org.ktorm:ktorm-core:3.6.0")
        implementation("org.ktorm:ktorm-jackson:3.6.0")
        implementation("org.ktorm:ktorm-support-mysql:3.6.0")
        implementation("org.mariadb.jdbc:mariadb-java-client:3.1.3")
        implementation("javax.xml.bind:jaxb-api:2.3.0")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
    configure<DependencyManagementExtension> {
        imports(delegateClosureOf<ImportsHandler> {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.1")
        })
    }
}

allprojects {
    group = "com.vgerbot"
    version = "1.0-SNAPSHOT"
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/grails-core")
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        mavenCentral()
    }
    tasks.withType<KotlinCompile> { // Settings for `KotlinCompile` tasks
        kotlinOptions { // Kotlin compiler options
            freeCompilerArgs = listOf("-Xjsr305=strict") // `-Xjsr305=strict` enables the strict mode for JSR-305 annotations
            jvmTarget = "17" // This option specifies the target version of the generated JVM bytecode
        }
    }
}
