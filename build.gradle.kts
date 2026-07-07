subprojects {
    group = "core.framework"
    version = "9.6.0"
    repositories {
        maven {
            url = uri("https://neowu.github.io/maven-repo/")
            content {
                includeGroup("core.framework.elasticsearch.module")
            }
        }
    }
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "latest"
}
