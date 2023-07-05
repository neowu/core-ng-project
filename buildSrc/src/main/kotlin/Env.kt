import org.gradle.api.Project

/**
 * @author neo
 */
class Env {
    companion object {
        // use gradlew -Penv=${env} to pass
        fun property(project: Project, name: String): String? {
            return if (project.hasProperty(name))
                project.properties[name] as String
            else null
        }
    }
}
