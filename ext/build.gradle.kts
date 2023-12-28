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
