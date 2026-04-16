plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.minecraftforge.net")
    maven(url = "https://maven.architectury.dev/")
    maven(url = "https://repo.essential.gg/repository/maven-public")
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("gg.essential:essential-gradle-toolkit:0.7.0-alpha.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("gg.essential:architectury-loom:1.15.50")
}

kotlin {
    jvmToolchain(21)
}
