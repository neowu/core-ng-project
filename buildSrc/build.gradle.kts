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

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.1.13")
    implementation("org.flywaydb:flyway-gradle-plugin:11.10.5")
    runtimeOnly("org.flywaydb:flyway-mysql:11.10.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.10.5")
}
