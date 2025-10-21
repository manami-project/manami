package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DefaultLatestVersionCheckerTest {

    @Test
    fun `posts event for a new version`() {
        // given
        val testCurrentVersionProvider = object: VersionProvider {
            override fun version(): SemanticVersion = SemanticVersion("3.0.0")
        }

        val testLatestVersionProvider = object: VersionProvider {
            override fun version(): SemanticVersion = SemanticVersion("3.2.2")
        }

        val events = mutableListOf<Event>()
        val testEventBus = object : EventBus by TestEventBus {
            override fun post(event: Event) {
                events.add(event)
            }
        }

        val versionChecker = DefaultLatestVersionChecker(
            currentVersionProvider = testCurrentVersionProvider,
            latestVersionProvider = testLatestVersionProvider,
            eventBus = testEventBus,
        )

        // when
        versionChecker.checkLatestVersion()

        // then
        assertThat(events).hasSize(1)

        val event = events.first()
        assertThat(event).isInstanceOf(NewVersionAvailableEvent::class.java)

        assertThat((event as NewVersionAvailableEvent).version).isEqualTo(SemanticVersion("3.2.2"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.0", "3.2.0"])
    fun `don't post an event if latest version is equal to or older than current version`(versionString: String) {
        // given
        val testCurrentVersionProvider = object: VersionProvider {
            override fun version(): SemanticVersion = SemanticVersion("3.2.0")
        }

        val testLatestVersionProvider = object: VersionProvider {
            override fun version(): SemanticVersion = SemanticVersion(versionString)
        }

        val events = mutableListOf<Event>()
        val testEventBus = object : EventBus by TestEventBus {
            override fun post(event: Event) {
                events.add(event)
            }
        }

        val versionChecker = DefaultLatestVersionChecker(
            currentVersionProvider = testCurrentVersionProvider,
            latestVersionProvider = testLatestVersionProvider,
            eventBus = testEventBus,
        )

        // when
        versionChecker.checkLatestVersion()

        // then
        assertThat(events).isEmpty()
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultLatestVersionChecker.instance

            // when
            val result = DefaultLatestVersionChecker.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultLatestVersionChecker::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}