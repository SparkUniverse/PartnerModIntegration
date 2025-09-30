pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.minecraftforge.net")
    }
    plugins {
        id("gg.essential.multi-version.root") version "0.6.10"
    }
}


rootProject.name = "PartnerModIntegration"
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.8.9-forge",
    "1.12.2-forge",
    "1.16.5-forge",
    "1.16.5-fabric",
    "1.17.1-fabric",
    "1.17.1-forge",
    "1.18.1-fabric",
    "1.18.2-fabric",
    "1.18.2-forge",
    "1.19-fabric",
    "1.19.2-fabric",
    "1.19.2-forge",
    "1.19.3-fabric",
    "1.19.3-forge",
    "1.19.4-fabric",
    "1.19.4-forge",
    "1.20-fabric",
    "1.20.1-fabric",
    "1.20.1-forge",
    "1.20.2-fabric",
    "1.20.2-forge",
    "1.20.4-fabric",
    "1.20.4-forge",
    "1.20.4-neoforge",
    "1.20.6-fabric",
    "1.20.6-forge",
    "1.20.6-neoforge",
    "1.21.1-fabric",
    "1.21.1-forge",
    "1.21.1-neoforge",
    "1.21.3-fabric",
    "1.21.3-forge",
    "1.21.3-neoforge",
    "1.21.4-fabric",
    "1.21.4-forge",
    "1.21.4-neoforge",
    "1.21.5-fabric",
    "1.21.5-forge",
    "1.21.5-neoforge",
    "1.21.6-fabric",
    "1.21.7-fabric",
    "1.21.7-neoforge",
    "1.21.8-fabric",
    "1.21.8-neoforge",
    "1.21.9-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}
