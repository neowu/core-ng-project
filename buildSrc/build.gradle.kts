plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.26")
    implementation("org.flywaydb:flyway-gradle-plugin:10.21.0")
    implementation("org.flywaydb:flyway-mysql:10.21.0")
}
