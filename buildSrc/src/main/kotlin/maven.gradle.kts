plugins {
    java
    `maven-publish`
}

val mavenURL = providers.gradleProperty("mavenURL").orNull    // usage: "gradlew -PmavenURL=/path clean publish"

if (mavenURL != null) {
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


