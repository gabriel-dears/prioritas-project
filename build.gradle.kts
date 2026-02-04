plugins {
    base
}

tasks.register("cleanAll") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

allprojects {
    repositories {
        mavenCentral()
    }
}