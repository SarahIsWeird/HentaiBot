import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.sarahisweird"
version = "1.0"

repositories {
    mavenCentral()
}

val discordKtVersion: String by project
val okHttpVersion: String by project
val klaxonVersion: String by project

dependencies {
    implementation("me.jakejmattson:DiscordKt:$discordKtVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.beust:klaxon:$klaxonVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}