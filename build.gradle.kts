plugins {
    kotlin("jvm") version "1.9.20"
    id("io.ktor.plugin") version "2.3.5"
}

group = "com.maxim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Фреймворк Ktor для обработки сетевых запросов от телефона
    implementation("io.ktor:ktor-server-core-jvm:2.3.5")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.5")

    // Драйвер для подключения к базе PostgreSQL 16
    implementation("org.postgresql:postgresql:42.6.0")

    // Логирование процессов сервера в консоли
    implementation("ch.qos.logback:logback-classic:1.4.11")
}