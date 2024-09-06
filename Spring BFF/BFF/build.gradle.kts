import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
}

group = "com.frontiers"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    // needed for the spring oidc addons
    maven { url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/") }
}

extra["springCloudVersion"] = "2023.0.3"

dependencies {

    /* kotlin co-routines */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    /* kotlin reflection */
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /* kotlin standard library with JDK 8 extensions */
    implementation(kotlin("stdlib-jdk8"))

    /* redis cache */
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    /* spring starter */
    implementation("org.springframework.boot:spring-boot-starter")

    /* spring session */
//    implementation("org.springframework.session:spring-session-core")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.apache.commons:commons-pool2:2.11.1")

    /* spring cloud gateway */
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    /* spring actuator */
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    /* spring circuit breaker */
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    /* spring security - essentials */
    implementation("org.springframework.boot:spring-boot-starter-security")

    /* spring security - oauth2 */
    // OAuth2 client functionality
    implementation("org.springframework.security:spring-security-oauth2-client")
    // JSON Object Signing and Encryption (JOSE).
    implementation("org.springframework.security:spring-security-oauth2-jose")

    /* spring other */
    // Jakarta Servlet API
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    // Java DSL for reading JSON documents
    implementation("com.jayway.jsonpath:json-path:2.9.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}