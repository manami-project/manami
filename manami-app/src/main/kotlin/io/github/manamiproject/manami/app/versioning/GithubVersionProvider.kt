package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import kotlinx.coroutines.runBlocking
import java.net.URI

internal class GithubVersionProvider(
    private val uri: URI = URI("https://github.com/manami-project/manami/releases/latest"),
    private val httpClient: HttpClient = DefaultHttpClient.instance,
): VersionProvider {

    override fun version(): SemanticVersion {
        val response = runBlocking { httpClient.get(uri.toURL()) }
        check(response.isOk()) { "Unable to check latest version, because response code wasn't 200." }

        val title = Regex("<title>.*?</title>").find(response.bodyAsText)?.value ?: EMPTY
        val rawVersion = Regex("([0-9]+\\.?){3}").find(title)?.value ?: EMPTY
        return  SemanticVersion(rawVersion)
    }
}