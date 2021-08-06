package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.logging.LoggerDelegate

internal class DefaultLatestVersionChecker(
    private val currentVersionProvider: VersionProvider = ResourceBasedVersionProvider,
    private val latestVersionProvider: VersionProvider = GithubVersionProvider(),
    private val eventBus: EventBus = SimpleEventBus,
) : LatestVersionChecker {

    override fun checkLatestVersion() {
        log.info("Checking if there is a new version available.")
        val currentVersion = currentVersionProvider.version()
        val latestVersion = latestVersionProvider.version()

        if (latestVersion.isNewerThan(currentVersion)) {
            log.info("Found new version [{}]", latestVersion)
            eventBus.post(NewVersionAvailableEvent(latestVersion))
        }
    }

    companion object {
        private val log by LoggerDelegate()
    }
}