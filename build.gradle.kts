import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "com.sarahisweird"
version = "1.5.0"

repositories {
    mavenCentral()
}

val discordKtVersion: String by project
val okHttpVersion: String by project
val klaxonVersion: String by project
val scrimageVersion: String by project
val exposedVersion: String by project
val mysqlConnectorVersion: String by project

dependencies {
    implementation("me.jakejmattson:DiscordKt:$discordKtVersion")

    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

    implementation("com.beust:klaxon:$klaxonVersion")

    implementation("com.sksamuel.scrimage:scrimage-core:$scrimageVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("mysql:mysql-connector-java:$mysqlConnectorVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    group = "build"

    manifest {
        attributes["Implementation-Title"] = "HentaiBot"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "com.sarahisweird.hentaibot.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) })

    with(tasks.jar.get() as CopySpec)
}

tasks {
    build {
        dependsOn(fatJar)
    }
}