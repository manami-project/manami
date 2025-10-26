package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update

internal class DefaultLatestVersionChecker(
    private val currentVersionProvider: VersionProvider = ResourceBasedVersionProvider,
    private val latestVersionProvider: VersionProvider = GithubVersionProvider(),
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : LatestVersionChecker {

    override suspend fun checkLatestVersion() {
        log.info { "Checking if there is a new version available." }
        val currentVersion = currentVersionProvider.version()
        val latestVersion = latestVersionProvider.version()

        if (latestVersion.isNewerThan(currentVersion)) {
            log.info { "Found new version [$latestVersion]" }
            eventBus.dashboardState.update { current -> current.copy(newVersion = latestVersion) }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultLatestVersionChecker]
         * @since 4.0.0
         */
        val instance: DefaultLatestVersionChecker by lazy { DefaultLatestVersionChecker() }
    }
}