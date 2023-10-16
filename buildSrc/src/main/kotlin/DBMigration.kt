import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.Properties

/**
 * @author neo
 */
object DBMigration {
    fun loadProperties(propertyFile: File): Map<String, String> {
        if (!propertyFile.exists()) throw Error("$propertyFile does not exist")
        val properties = Properties()
        FileInputStream(propertyFile).use {
            properties.load(it)
        }
        return properties.map { (key, value) ->
            key as String to value as String
        }.toMap()
    }

    // refer to core.framework.internal.db.cloud.GCloudAuthProvider
    fun iamUser(): String {
        val email = metadata("email")
        val regex = """([^@]*)@[^@]*""".toRegex()
        return regex.matchEntire(email)!!.groups[1]!!.value
    }

    fun iamAccessToken(): String {
        val tokenJSON = metadata("token")
        val regex = """\{"access_token":"([^"]*)",.*""".toRegex()
        return regex.matchEntire(tokenJSON)!!.groups[1]!!.value
    }

    private fun metadata(attribute: String): String {
        val conn = URI("http://169.254.169.254/computeMetadata/v1/instance/service-accounts/default/${attribute}").toURL().openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Metadata-Flavor", "Google")
        conn.connectTimeout = 500
        conn.readTimeout = 1000
        val statusCode = conn.responseCode
        if (statusCode != 200) throw Error("failed to fetch gcloud iam metadata, status=${statusCode}")
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
