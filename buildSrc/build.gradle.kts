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
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.5.8")
    implementation("org.flywaydb:flyway-gradle-plugin:12.10.0")
    runtimeOnly("org.flywaydb:flyway-mysql:12.10.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:12.10.0")
}
