import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("java")
    id("org.springframework.boot") version "2.0.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

apply(from = "../../config/it-config.gradle.kts")
apply(plugin = "io.spring.dependency-management")

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.1.6.RELEASE")
    }
}

dependencies {
    implementation(project(":nixer-plugin-core"))
    implementation(project(":nixer-plugin-captcha"))
    implementation(project(":nixer-plugin-pwned-check"))
    implementation("org.springframework.boot", "spring-boot")
    implementation("org.springframework.boot", "spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-security")
    implementation("org.springframework.boot", "spring-boot-starter-web")

    implementation("io.micrometer", "micrometer-registry-influx", "1.2.0")
    runtimeOnly("com.h2database", "h2")

    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310")

    testImplementation("org.springframework", "spring-test")
    testImplementation("org.springframework.integration", "spring-integration-test")
    testImplementation("org.springframework.security", "spring-security-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-test") {
        exclude(module = "junit")
    }
}

tasks.getByName<BootJar>("bootJar") {
    mainClassName = "eu.xword.nixer.example.NixerPluginApplication"
}

tasks.getByName<BootRun>("bootRun") {
    main = "eu.xword.nixer.example.NixerPluginApplication"
}