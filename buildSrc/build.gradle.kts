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
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.2.5")
    implementation("org.flywaydb:flyway-gradle-plugin:10.1.0")
    implementation("org.flywaydb:flyway-mysql:10.1.0")
}
