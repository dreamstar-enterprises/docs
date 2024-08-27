import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    application
}

application {
    mainClass.set("com.example.reverseproxy.ReverseProxyApplicationKt")
}

group = "com.example"
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
}

extra["springCloudVersion"] = "2023.0.3"

dependencies {
    /* kotlin co-routines */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    /* kotlin reflection */
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /* spring web */
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    /* spring cloud gateway */
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    /* test - dependencies */
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

//    testImplementation("io.projectreactor:reactor-test")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation(kotlin("stdlib-jdk8"))

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


tasks.withType<Test> {
    enabled = false
    useJUnitPlatform()
}

tasks.withType<Jar> {

    // specify JAR file name
    archiveBaseName.set("ReverseProxy")
    archiveVersion.set("0.0.1-SNAPSHOT")

    // ensure that both compiled classes and resources are included in the JAR file
    from(sourceSets.main.get().output)
    sourceSets.main.configure {
        resources.srcDirs("src/main/").includes.addAll(arrayOf("**/*.*"))
    }

    // include dependencies in the JAR file
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    // specify location of main class
    manifest {
        attributes["Main-Class"] = "com.example.reverseproxy.ReverseProxyApplicationKt"
    }

    // define duplicate strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // exclude specific signed files from being included in the final JAR
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("bootJar") {
    dependsOn("jar")
}
tasks.named("bootJar") {
    mustRunAfter("jar")
}
tasks.named("bootDistZip") {
    dependsOn("jar")
}
tasks.named("bootDistZip") {
    mustRunAfter("jar")
}
tasks.named("bootDistTar") {
    dependsOn("jar")
}
tasks.named("bootDistTar") {
    mustRunAfter("jar")
}
tasks.named("startScripts") {
    dependsOn("bootJar")
}
tasks.named("startScripts") {
    mustRunAfter("bootJar")
}