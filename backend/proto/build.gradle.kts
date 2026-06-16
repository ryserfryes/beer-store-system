plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

dependencies {
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.kotlin.stub)
    api(libs.protobuf.kotlin)
    implementation(libs.kotlin.reflect)
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:4.28.3" }
    plugins {
        create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.68.1" }
        create("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar" }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins { create("kotlin") }
        }
    }
}
