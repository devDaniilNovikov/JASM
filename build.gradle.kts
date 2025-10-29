import org.gradle.internal.impldep.org.bouncycastle.oer.OERDefinition.optional

plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}
val springCloudVersion by extra("2025.0.0")

group = "dn"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.integration:spring-integration-http")
    implementation("org.springframework.integration:spring-integration-jpa")
    implementation("org.springframework.integration:spring-integration-kafka")
    implementation("org.springframework.integration:spring-integration-mail")
    implementation("org.springframework.integration:spring-integration-redis")
    // https://mvnrepository.com/artifact/org.springframework/spring-context-indexer
    implementation("org.springframework:spring-context-indexer:7.0.0-M9")
    implementation ("com.stripe:stripe-java:24.0.0")
    // https://mvnrepository.com/artifact/redis.clients/jedis
    // https://mvnrepository.com/artifact/io.lettuce/lettuce-core
    implementation("io.lettuce:lettuce-core:6.7.1.RELEASE")
    implementation ("org.apache.pdfbox:pdfbox:2.0.30")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.13.2")

    // https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-ui

    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")

    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.7.7")
    implementation ("redis.clients:jedis:5.1.0")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.integration:spring-integration-websocket")
    implementation("org.springframework.integration:spring-integration-stomp")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.integration:spring-integration-amqp")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    // https://mvnrepository.com/artifact/com.speedment.jpastreamer/jpastreamer-core
    runtimeOnly("org.postgresql:postgresql:42.7.7")
    compileOnly ("org.projectlombok:lombok:1.18.32")
    annotationProcessor ("org.projectlombok:lombok:1.18.32")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

