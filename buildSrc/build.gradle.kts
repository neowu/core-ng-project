plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        // kotlin doesn't support Java 24 yet
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.1.13")
    implementation("org.flywaydb:flyway-gradle-plugin:11.2.0")
    implementation("org.flywaydb:flyway-mysql:11.2.0")
}
