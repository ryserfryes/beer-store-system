plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

dependencies {
    implementation(libs.spring.boot.web)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.kotlin)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> { useJUnitPlatform() }
