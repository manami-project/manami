package io.github.manamiproject.manami.app.search

import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class SearchConfigTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if min year is before the year of the first anime`() {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                SearchConfig(
                    metaDataProvider = "myanimelist.net",
                    year = 1900..2025,
                )
            }

            // then
            assertThat(result).hasMessage("Invalid year range. Minimum cannot be before the year of the first anime [1907]")
        }
    }
}