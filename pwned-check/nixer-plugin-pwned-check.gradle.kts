plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.spring.dependency-management") version "1.0.6.RELEASE" // FIXME duplicates declaration in 'core'
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.1.6.RELEASE") // FIXME duplicates declaration in 'core'
    }
}

dependencies {
    annotationProcessor("org.springframework.boot", "spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot", "spring-boot-configuration-processor")

    implementation(project(":nixer-plugin-core"))
    implementation(project(":bloom-filter"))

    implementation("org.springframework", "spring-web")
}