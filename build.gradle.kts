plugins {
    `java-library`
    `maven-publish`
}

apply(plugin = "project")

subprojects {
    group = "core.framework"
    version = "9.1.8"
}

val elasticVersion = "8.15.0"
val jacksonVersion = "2.17.2"
val junitVersion = "5.12.1"
val mockitoVersion = "5.16.1"
val assertjVersion = "3.27.3"

project("core-ng-api") {
    apply(plugin = "lib")
}

project("core-ng") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng-api"))
        api("org.slf4j:slf4j-api:2.0.16")
        implementation("org.javassist:javassist:3.30.2-GA")
        implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${jacksonVersion}")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
        implementation("com.squareup.okhttp3:okhttp:4.12.0@jar")
        implementation("com.squareup.okio:okio:3.2.0")              // okio 3.3.0 has synchronization issue with virtual thread
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
        implementation("io.undertow:undertow-core:2.3.18.Final")
        implementation("org.apache.kafka:kafka-clients:4.0.0")
        compileOnly("org.jboss.logging:jboss-logging-annotations:2.2.1.Final")
        compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.3")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        testImplementation("org.mockito:mockito-junit-jupiter:${mockitoVersion}")
        testImplementation("org.assertj:assertj-core:${assertjVersion}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("org.hsqldb:hsqldb:2.7.3")
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
        testRuntimeOnly("org.hsqldb:hsqldb:2.7.3")
    }
}

project("core-ng-mongo") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng"))
        api("org.mongodb:mongodb-driver-sync:5.2.1")
        testImplementation(project(":core-ng-test"))
    }
}

project("core-ng-mongo-test") {
    apply(plugin = "lib")
    dependencies {
        implementation(project(":core-ng-test"))
        implementation(project(":core-ng-mongo"))
        implementation("de.bwaldvogel:mongo-java-server:1.46.0")
    }
}

project("core-ng-search") {
    apply(plugin = "lib")
    dependencies {
        api(project(":core-ng"))
        api("co.elastic.clients:elasticsearch-java:${elasticVersion}")
        implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        testImplementation(project(":core-ng-test"))
    }
}

project("core-ng-search-test") {
    apply(plugin = "lib")
    dependencies {
        implementation(project(":core-ng-test"))
        implementation(project(":core-ng-search"))
        implementation("org.elasticsearch:elasticsearch:${elasticVersion}")
        implementation("org.elasticsearch.plugin:transport-netty4:${elasticVersion}")
        implementation("org.codelibs.elasticsearch.module:mapper-extras:${elasticVersion}")         // used by elasticsearch scaled_float
        implementation("org.codelibs.elasticsearch.module:lang-painless:${elasticVersion}")
        implementation("org.codelibs.elasticsearch.module:analysis-common:${elasticVersion}")       // used by elasticsearch stemmer
        implementation("org.codelibs.elasticsearch.module:reindex:${elasticVersion}@jar")           // used by elasticsearch deleteByQuery
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
