plugins {
    kotlin("jvm") version "1.9.24"
    `java-library`
}

group = "br.com.fiap.adj8.phase5.prioritas"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
    implementation(kotlin("stdlib"))
}

tasks.jar {
    enabled = true
}