package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import org.xml.sax.Attributes
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.net.URI
import java.net.URLDecoder
import java.nio.file.Path
import javax.xml.parsers.SAXParserFactory
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.text.Charsets.UTF_8

internal class FileParser(
    cache: AnimeCache = Caches.defaultAnimeCache,
) : Parser<ParsedManamiFile> {

    private val saxParser = SAXParserFactory.newInstance().apply { isValidating = true }.newSAXParser()
    private val versionHandler = ManamiVersionHandler()
    private val documentHandler = ManamiFileHandler(cache)

    override fun handlesSuffix(): FileSuffix = "xml"

    override fun parse(file: RegularFile): ParsedManamiFile {
        require(file.regularFileExists()) { "Given path [${file.toAbsolutePath()}] is either not a file or doesn't exist." }
        require(file.fileSuffix() == handlesSuffix()) { "Parser doesn't support given file suffix." }

        val entityResolver = EntityResolver { _, systemId ->
            val fileName = Path(systemId).fileName
            InputSource(file.parent.resolve(fileName).toString())
        }

        versionHandler.entityResolver = entityResolver
        saxParser.parse(file.inputStream(), versionHandler)
        require(versionHandler.version == minVersion || versionHandler.version.isNewerThan(minVersion)) { "Unable to parse manami file older than $minVersion" }

        documentHandler.entityResolver = entityResolver
        saxParser.parse(file.inputStream(), documentHandler)
        return documentHandler.parsedFile
    }

    private companion object {
        private val minVersion = SemanticVersion("3.0.0")
    }
}

private class ManamiFileHandler(private val cache: AnimeCache) : DefaultHandler() {

    private var strBuilder = StringBuilder()
    private val animeListEntries = mutableSetOf<AnimeListEntry>()
    private val watchListEntries = mutableSetOf<WatchListEntry>()
    private val ignoreListEntries = mutableSetOf<IgnoreListEntry>()

    private var _parsedFile = ParsedManamiFile()
    val parsedFile
        get() = _parsedFile

    var entityResolver: EntityResolver = EntityResolver { _, _ -> InputSource(EMPTY) }

    override fun resolveEntity(publicId: String?, systemId: String?): InputSource = entityResolver.resolveEntity(publicId, systemId)

    override fun characters(ch: CharArray, start: Int, length: Int) {
        strBuilder.append(String(ch, start, length))
    }

    override fun startElement(namespaceUri: String, localName: String, qName: String, attributes: Attributes) {
        strBuilder = StringBuilder()

        when (qName) {
            "animeListEntry" -> createAnimeEntry(attributes)
            "watchListEntry" -> createWatchListEntry(attributes)
            "ignoreListEntry" -> createIgnoreListEntry(attributes)
        }
    }

    private fun createAnimeEntry(attributes: Attributes) {
        val link = attributes.getValue("link").trim().let {
            if (it.isBlank()) {
                NoLink
            } else {
                Link(it)
            }
        }

        animeListEntries.add(
            AnimeListEntry(
                link = link,
                title = attributes.getValue("title").trim(),
                thumbnail = URI(attributes.getValue("thumbnail").trim()),
                episodes = attributes.getValue("episodes").trim().toInt(),
                type = Anime.Type.valueOf(attributes.getValue("type").trim().uppercase()),
                location = parseLocation(attributes.getValue("location")),
            )
        )
    }

    private fun createWatchListEntry(attributes: Attributes) {
        val link = attributes.getValue("link").trim().let {
            if (it.isBlank()) {
                throw IllegalStateException("Link must not be blank")
            } else {
                Link(it)
            }
        }

        watchListEntries.add(
            WatchListEntry(
                link = link,
                title = attributes.getValue("title").trim(),
                thumbnail = URI(attributes.getValue("thumbnail").trim()),
                status = (cache.fetch(link.uri) as PresentValue<Anime>).value.status
            )
        )
    }

    private fun createIgnoreListEntry(attributes: Attributes) {
        val link = attributes.getValue("link").trim().let {
            if (it.isBlank()) {
                throw IllegalStateException("Link must not be blank")
            } else {
                Link(it)
            }
        }

        ignoreListEntries.add(
            IgnoreListEntry(
                link = link,
                title = attributes.getValue("title").trim(),
                thumbnail = URI(attributes.getValue("thumbnail").trim()),
            )
        )
    }

    private fun parseLocation(value: String): Path {
        val trimmedLocation = value.trim()

        val location = when(trimmedLocation.contains('%')) {
            true -> {
                try {
                    URLDecoder.decode(trimmedLocation, UTF_8) // necessary for locations from manami <= 3.6.1
                } catch(e: IllegalArgumentException) {
                    trimmedLocation
                }
            }
            false -> trimmedLocation
        }

        return Path(location)
    }

    override fun endDocument() {
        _parsedFile = ParsedManamiFile(
            animeListEntries = animeListEntries,
            watchListEntries = watchListEntries,
            ignoreListEntries = ignoreListEntries,
        )
    }
}