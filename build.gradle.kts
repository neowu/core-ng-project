plugins {
    `java-library`
    `maven-publish`
    project
}

subprojects {
    group = "core.framework"
    version = "9.3.0-b2"
    repositories {
        maven {
            url = uri("https://neowu.github.io/maven-repo/")
            content {
                includeGroup("core.framework.elasticsearch.module")
            }
        }
    }
}

val elasticVersion = "8.18.1"
val jacksonVersion = "2.20.0"
val junitVersion = "5.13.4"
val mockitoVersion = "5.20.0"
val assertjVersion = "3.27.6"

project("core-ng-api") {
    apply(plugin = "lib")
    dependencies {
        api("org.jspecify:jspecify:1.0.0")
    }
}

project("core-ng") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng-api"))
        api("org.slf4j:slf4j-api:2.0.17")
        implementation("org.javassist:javassist:3.30.2-GA")
        implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${jacksonVersion}")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
        implementation("com.squareup.okhttp3:okhttp:5.1.0")
        implementation("io.undertow:undertow-core:2.3.19.Final")
        implementation("org.apache.kafka:kafka-clients:4.1.0") {
            exclude("org.xerial.snappy")
            exclude("org.lz4")
        }
        compileOnly("org.jboss.logging:jboss-logging-annotations:2.2.1.Final")
        compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.3")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        testImplementation("org.mockito:mockito-junit-jupiter:${mockitoVersion}")
        testImplementation("org.assertj:assertj-core:${assertjVersion}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("org.hsqldb:hsqldb:2.7.4")
    }
}

project("core-ng-test") {
    apply(plugin = "lib")
    dependencies {
        api("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        api("org.mockito:mockito-junit-jupiter:${mockitoVersion}")
        api("org.assertj:assertj-core:${assertjVersion}")
        implementation(project(":core-ng"))
        implementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        implementation("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("org.hsqldb:hsqldb:2.7.4")
    }
}

project("core-ng-mongo") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng"))
        api("org.mongodb:mongodb-driver-sync:5.5.1")
        testImplementation(project(":core-ng-test"))
    }
}

project("core-ng-mongo-test") {
    apply(plugin = "lib")
    dependencies {
        implementation(project(":core-ng-test"))
        implementation(project(":core-ng-mongo"))
        implementation("de.bwaldvogel:mongo-java-server:1.47.0")
    }
}

project("core-ng-search") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng"))
        api("co.elastic.clients:elasticsearch-java:${elasticVersion}") {
            exclude(group = "io.opentelemetry")
        }
        implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        testImplementation(project(":core-ng-test"))
    }
}

project("core-ng-search-test") {
    apply(plugin = "lib")
    dependencies {
        implementation(project(":core-ng-test"))
        implementation(project(":core-ng-search"))
        implementation("org.elasticsearch:elasticsearch:${elasticVersion}") {
            exclude(group = "io.opentelemetry")
        }
        implementation("org.elasticsearch.plugin:transport-netty4:${elasticVersion}")
        implementation("core.framework.elasticsearch.module:mapper-extras:${elasticVersion}")       // used by elasticsearch scaled_float
        implementation("core.framework.elasticsearch.module:lang-painless:${elasticVersion}")
        implementation("core.framework.elasticsearch.module:analysis-common:${elasticVersion}")     // used by elasticsearch stemmer
        implementation("core.framework.elasticsearch.module:reindex:${elasticVersion}")             // used by elasticsearch deleteByQuery
        runtimeOnly("org.apache.logging.log4j:log4j-to-slf4j:2.19.0")
    }
}

val mavenURL = project.properties["mavenURL"] as String?    // usage: "gradlew -PmavenURL=/path clean publish"

subprojects {
    if (mavenURL != null && project.name.startsWith("core-ng")) {
        apply(plugin = "maven-publish")

        val mavenDir = file(mavenURL)
        assert(mavenDir.exists())
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
            repositories {
                maven { url = uri(mavenURL) }
            }
        }
    }
}
