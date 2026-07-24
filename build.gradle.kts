subprojects {
    group = "core.framework"
    version = "9.6.1-b1"
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
