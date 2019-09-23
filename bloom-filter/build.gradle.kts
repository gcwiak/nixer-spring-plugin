plugins {
    java
}

val jacksonVersion: String = "2.7.8"

dependencies {
    implementation("com.google.guava:guava:22.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")

    testImplementation("junit:junit:4.12")
    testImplementation("org.assertj:assertj-core:3.6.2")
}
