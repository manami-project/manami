package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.versioning.ResourceBasedVersionProvider
import io.github.manamiproject.manami.app.versioning.VersionProvider
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL

internal class DefaultFileWriter(
    private val state: State = InternalState,
    private val versionProvider: VersionProvider = ResourceBasedVersionProvider,
) : FileWriter {

    override suspend fun writeTo(file: RegularFile) {
        val manamiFile = SerializableManamiFile(
            version = versionProvider.version().toString(),
            animeListEntries = state.animeList().map {
                SerializableAnimeListEntry(
                    link = it.link.toString(),
                    title = it.title,
                    thumbnail = it.thumbnail.toString(),
                    episodes = it.episodes.toString(),
                    type = it.type.toString(),
                    location = it.location.toString(),
                )
            }.sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes })),
            watchListEntries = state.watchList().map { it.link.uri.toString() }.sorted(),
            ignoreListEntries = state.ignoreList().map { it.link.uri.toString() }.sorted(),
        )

        Json.toJson(manamiFile, DEACTIVATE_SERIALIZE_NULL).writeToFile(file, true)
    }
    
    companion object {

        /**
         * Singleton of [DefaultFileWriter]
         * @since 4.0.0
         */
        val instance: DefaultFileWriter by lazy { DefaultFileWriter() }
    }
}