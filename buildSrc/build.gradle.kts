plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    compilerOptions {
        // kotlin not support jvm 25 yet
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
    }
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.2.5")
    implementation("org.flywaydb:flyway-gradle-plugin:11.13.2")
    runtimeOnly("org.flywaydb:flyway-mysql:11.13.2")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.13.2")
}
