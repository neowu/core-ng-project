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
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.1.2")
    implementation("org.flywaydb:flyway-gradle-plugin:11.2.0")
    implementation("org.flywaydb:flyway-mysql:11.2.0")
}
