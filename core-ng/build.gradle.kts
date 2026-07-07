plugins {
    project
    lint
    lib
    maven
}

dependencies {
    api(project(":core-ng-api"))
    api("org.slf4j:slf4j-api:2.0.17")
    api("org.jspecify:jspecify:1.0.0")
    implementation("org.javassist:javassist:3.31.0-GA")
    implementation(libs.jackson.afterburner)
    implementation(libs.jackson.databind)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("io.undertow:undertow-core:2.4.1.Final")
    implementation("org.apache.kafka:kafka-clients:4.3.1") {
        exclude("org.xerial.snappy")
        exclude("at.yawk.lz4")
    }
    compileOnly("org.jboss.logging:jboss-logging-annotations:3.0.4.Final")
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
    testImplementation(libs.junit.api)
    testImplementation(libs.mockito)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly(libs.hsqldb)
}
