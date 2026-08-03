plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.fettqa.events"
version = "0.0.1-SNAPSHOT"

val allureVersion = "2.29.1"
val aspectJVersion = "1.9.22"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val agent: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = true
}

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectJVersion")

    testImplementation("io.appium:java-client:9.3.0")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.25.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-junit5")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${agent.singleFile}")
    systemProperty("appiumUrl", System.getProperty("appiumUrl", "http://127.0.0.1:4723"))
    System.getProperty("apk")?.let { systemProperty("apk", it) }
}
