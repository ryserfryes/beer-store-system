plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

dependencies {
    implementation(project(":proto"))
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.data.jpa)
    implementation(libs.spring.boot.kafka)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.kotlin)
    implementation(libs.postgresql)
    implementation(libs.grpc.server.starter)
    implementation(libs.grpc.client.starter)
    implementation(libs.grpc.netty)
    implementation(libs.springdoc.openapi.ui)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> { useJUnitPlatform() }
