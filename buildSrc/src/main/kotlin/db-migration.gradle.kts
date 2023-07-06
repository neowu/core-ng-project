import org.flywaydb.gradle.task.AbstractFlywayTask

plugins {
    id("org.flywaydb.flyway")
}

tasks.withType<AbstractFlywayTask> {
    doFirst {
        flyway {
            configurations = arrayOf("runtimeClasspath") // use runtimeOnly scope in actual db-migration project
            placeholderReplacement = false
            assert(file("src/main/resources/db/migration").exists())

            val env = project.properties["env"] // use gradlew -Penv=${env} to pass
            val propertyFile = if (env == null) file("src/main/resources/flyway.properties") else file("conf/${env}/resources/flyway.properties")
            val properties = DBMigration.loadProperties(propertyFile)

            url = properties["flyway.url"]

            val userValue = properties["flyway.user"]
            if (userValue == "iam/gcloud") {
                user = DBMigration.iamUser()
                password = DBMigration.iamAccessToken()
            } else {
                user = userValue
                password = properties["flyway.password"]
            }
        }
    }
}
