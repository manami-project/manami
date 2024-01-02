package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.cache.TestHttpClient
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.loadTestResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL

internal class GithubVersionProviderTest {

    @Test
    fun `correctly extract version`() {
        // given
        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                return HttpResponse(
                    code = 200,
                    body = loadTestResource("versioning_tests/github_versioning_tests/latest_version_page.html").toByteArray(),
                )
            }
        }

        val versionProvider = GithubVersionProvider(
            httpClient = testHttpClient,
        )

        // when
        val result = versionProvider.version()

        // then
        assertThat(result).isEqualTo(SemanticVersion("3.2.2"))
    }

    @Test
    fun `throws exception if version cannot be extracted`() {
        // given
        val otherBody = """
            <html>
             <head>
                <title>Latest Release</title>
            </head>
            <bod>Some content</body>
            </html>
        """.trimIndent()

        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                return HttpResponse(
                    code = 200,
                    body = otherBody.toByteArray(),
                )
            }
        }

        val versionProvider = GithubVersionProvider(
            httpClient = testHttpClient,
        )

        // when
        val result = assertThrows<IllegalArgumentException> {
            versionProvider.version()
        }

        // then
        assertThat(result).hasMessage("Version must be of format NUMBER.NUMBER.NUMBER")
    }

    @Test
    fun `throws exception if response code is not 200`() {
        // given
        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                return HttpResponse(
                    code = 429,
                    body = EMPTY.toByteArray(),
                )
            }
        }

        val versionProvider = GithubVersionProvider(
            httpClient = testHttpClient,
        )

        // when
        val result = assertThrows<IllegalStateException> {
            versionProvider.version()
        }

        // then
        assertThat(result).hasMessage("Unable to check latest version, because response code wasn't 200.")
    }
}