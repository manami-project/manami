package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.DashboardState
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.AfterTest

internal class DefaultLatestVersionCheckerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Test
    fun `posts event for a new version`() {
        runBlocking {
            // given
            val receivedEvents = mutableListOf<DashboardState>()
            val eventCollector = launch { CoroutinesFlowEventBus.dashboardState.collect { event -> receivedEvents.add(event) } }
            delay(100)

            val testCurrentVersionProvider = object: VersionProvider {
                override fun version(): SemanticVersion = SemanticVersion("3.0.0")
            }

            val testLatestVersionProvider = object: VersionProvider {
                override fun version(): SemanticVersion = SemanticVersion("3.2.2")
            }

            val versionChecker = DefaultLatestVersionChecker(
                currentVersionProvider = testCurrentVersionProvider,
                latestVersionProvider = testLatestVersionProvider,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            versionChecker.checkLatestVersion()

            // then
            delay(100)
            eventCollector.cancelAndJoin()
            assertThat(receivedEvents).hasSize(2) // init, new version
            assertThat(receivedEvents.first().newVersion).isNull()
            assertThat(receivedEvents.last().newVersion).isEqualTo(SemanticVersion("3.2.2"))
            assertThat(receivedEvents.last()).isEqualTo(CoroutinesFlowEventBus.dashboardState.value)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.0", "3.2.0"])
    fun `don't post an event if latest version is equal to or older than current version`(versionString: String) {
        runBlocking {
            // given
            val receivedEvents = mutableListOf<DashboardState>()
            val eventCollector = launch { CoroutinesFlowEventBus.dashboardState.collect { event -> receivedEvents.add(event) } }
            delay(100)

            val testCurrentVersionProvider = object: VersionProvider {
                override fun version(): SemanticVersion = SemanticVersion("3.2.0")
            }

            val testLatestVersionProvider = object: VersionProvider {
                override fun version(): SemanticVersion = SemanticVersion(versionString)
            }

            val versionChecker = DefaultLatestVersionChecker(
                currentVersionProvider = testCurrentVersionProvider,
                latestVersionProvider = testLatestVersionProvider,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            versionChecker.checkLatestVersion()

            // then
            delay(100)
            eventCollector.cancelAndJoin()
            assertThat(receivedEvents).hasSize(1) // init event
            assertThat(receivedEvents.first().newVersion).isNull()
            assertThat(receivedEvents.last()).isEqualTo(CoroutinesFlowEventBus.dashboardState.value)
        }
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