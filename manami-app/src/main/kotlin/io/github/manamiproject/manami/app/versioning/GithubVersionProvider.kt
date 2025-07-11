package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.runBlocking
import java.net.URI

internal class GithubVersionProvider(
    private val uri: URI = URI("https://api.github.com/repos/manami-project/manami/releases/latest"),
    private val httpClient: HttpClient = DefaultHttpClient.instance,
): VersionProvider {

    override fun version(): SemanticVersion {
        return runBlocking {
            val response = httpClient.get(uri.toURL())
            check(response.isOk()) { "Unable to check latest version, because response code wasn't 200." }

            val version =  Json.parseJson<GithubResponse>(response.bodyAsString())!!.name
            SemanticVersion(version)
        }
    }
}

private data class GithubResponse(
    val name: String,
)