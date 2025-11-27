import org.flywaydb.gradle.task.AbstractFlywayTask

plugins {
    id("org.flywaydb.flyway")
}

tasks.withType<AbstractFlywayTask> {
    notCompatibleWithConfigurationCache("https://github.com/flyway/flyway/issues/4107")

    val migrationDir = file("src/main/resources/db/migration")
    if (!migrationDir.exists()) throw Error("$migrationDir does not exist")
    val env = properties["env"] // use gradlew -Penv=${env} to pass
    val propertyFile = if (env == null) file("src/main/resources/flyway.properties") else file("conf/${env}/resources/flyway.properties")
    val properties = DBMigration.loadProperties(propertyFile)

    doFirst {
        flyway {
            configurations = arrayOf("runtimeClasspath") // use runtimeOnly scope in actual db-migration project
            placeholderReplacement = false
            url = properties["flyway.url"]
            val userValue = properties["flyway.user"]
            if (userValue == "iam/gcloud") {
                val dialect = """jdbc:(\w+)://.+""".toRegex().matchEntire(url)!!.groups[1]!!.value
                user = DBMigration.iamUser(dialect)
                password = DBMigration.iamAccessToken()
            } else {
                user = userValue
                password = properties["flyway.password"]
            }
        }
    }
}
