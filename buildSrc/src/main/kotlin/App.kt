import java.io.File

/**
 * @author neo
 */
class App {
    companion object {
        fun replaceText(file: File, oldString: String, newString: String) {
            val content = file.readText()
            val updatedContent = content.replace(oldString, newString)
            file.writeText(updatedContent)
        }
    }
}
