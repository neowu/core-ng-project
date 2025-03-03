plugins {
    java
}

project("log-processor") {
    apply(plugin = "app")
    dependencies {
        implementation(project(":core-ng"))
        implementation(project(":core-ng-search"))
        testImplementation(project(":core-ng-test"))
        testImplementation(project(":core-ng-search-test"))
    }
}

project("log-collector") {
    apply(plugin = "app")
    dependencies {
        implementation(project(":core-ng"))
        testImplementation(project(":core-ng-test"))
    }
}

project("log-exporter") {
    apply(plugin = "app")
    dependencies {
        implementation(project(":core-ng"))

        // for parquet
        compileOnly("org.apache.hadoop:hadoop-annotations:3.4.1")
        implementation("org.apache.parquet:parquet-avro:1.15.0")
        implementation("org.apache.avro:avro:1.12.0")
        implementation("org.apache.hadoop:hadoop-common:3.4.1@jar")
        runtimeOnly("commons-collections:commons-collections:3.2.2@jar")
        runtimeOnly("com.fasterxml.woodstox:woodstox-core:5.4.0@jar")
        runtimeOnly("org.codehaus.woodstox:stax2-api:4.2.1@jar")
        runtimeOnly("org.apache.hadoop.thirdparty:hadoop-shaded-guava:1.2.0@jar")

        testImplementation(project(":core-ng-test"))
    }
}

project("monitor") {
    apply(plugin = "app")
    dependencies {
        implementation(project(":core-ng"))
        implementation(project(":core-ng-mongo"))
        testImplementation(project(":core-ng-test"))
        testImplementation(project(":core-ng-mongo-test"))
    }
}
