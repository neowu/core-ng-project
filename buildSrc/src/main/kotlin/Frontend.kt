import org.gradle.internal.os.OperatingSystem

/**
 * @author neo
 */
object Frontend {
    fun commandLine(commands: List<String>): List<String> {
        val commandLine = ArrayList<String>()
        val isWindows = OperatingSystem.current().isWindows
        if (isWindows) commandLine.addAll(listOf("cmd", "/c"))
        commandLine.addAll(commands)
        return commandLine
    }
}
