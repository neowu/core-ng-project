plugins {
    `java-library`
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

java {
    withSourcesJar()
}
