package io.github.manamiproject.manami.app.import.parser.manami

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class SemanticVersionTest {

    @ParameterizedTest
    @ValueSource(strings = ["test", "  ", EMPTY, "1", "1.1", "1.1.x", "1.1.1-BETA"])
    fun `throws exception if the given string is not valid`(value: String) {
        // when
        val result = assertThrows<IllegalArgumentException> {
            SemanticVersion(value)
        }

        // then
        assertThat(result).hasMessage("Version must be of format NUMBER.NUMBER.NUMBER")
    }

    @Test
    fun `correctly extract major version`() {
        // given
        val version = SemanticVersion("3.1.2")

        // when
        val result = version.major()

        // then
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `correctly extract minor version`() {
        // given
        val version = SemanticVersion("3.1.2")

        // when
        val result = version.minor()

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `correctly extract patch version`() {
        // given
        val version = SemanticVersion("3.1.2")

        // when
        val result = version.patch()

        // then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `toString returns the original string`() {
        // given
        val originalValue = "3.1.2"
        val version = SemanticVersion(originalValue)

        // when
        val result = version.toString()

        // then
        assertThat(result).isEqualTo(originalValue)
    }

    @Nested
    inner class IsNewerTests {

        @Test
        fun `returns false if both versions are equal`() {
            // given
            val version = SemanticVersion("3.1.2")

            // when
            val result = version.isNewerThan(version)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @ValueSource(strings = ["2.1.2", "3.0.2", "3.1.1"])
        fun `returns true if the current version is newer than the other`(value: String) {
            // given
            val version = SemanticVersion("3.1.2")
            val otherVersion = SemanticVersion(value)

            // when
            val result = version.isNewerThan(otherVersion)

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @ValueSource(strings = ["4.1.2", "3.2.2", "3.1.3"])
        fun `returns false if the current version is not newer than the other`(value: String) {
            // given
            val version = SemanticVersion("3.1.2")
            val otherVersion = SemanticVersion(value)

            // when
            val result = version.isNewerThan(otherVersion)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class IsOlderTests {

        @Test
        fun `returns false if both versions are equal`() {
            // given
            val version = SemanticVersion("3.1.2")

            // when
            val result = version.isOlderThan(version)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @ValueSource(strings = ["2.1.2", "3.0.2", "3.1.1"])
        fun `returns false if the current version is not older than the other`(value: String) {
            // given
            val version = SemanticVersion("3.1.2")
            val otherVersion = SemanticVersion(value)

            // when
            val result = version.isOlderThan(otherVersion)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @ValueSource(strings = ["4.1.2", "3.2.2", "3.1.3"])
        fun `returns true if the current version is older than the other`(value: String) {
            // given
            val version = SemanticVersion("3.1.2")
            val otherVersion = SemanticVersion(value)

            // when
            val result = version.isOlderThan(otherVersion)

            // then
            assertThat(result).isTrue()
        }
    }
}