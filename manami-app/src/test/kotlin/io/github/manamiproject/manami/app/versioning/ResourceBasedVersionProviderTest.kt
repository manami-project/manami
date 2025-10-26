package io.github.manamiproject.manami.app.versioning

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ResourceBasedVersionProviderTest {

    @Test
    fun `default version is 3-0-0 - the version is overwritten during release build`() {
        runBlocking {
            // when
            val result = ResourceBasedVersionProvider.version()

            // then
            assertThat(result.toString()).isEqualTo("3.0.0")
        }
    }
}