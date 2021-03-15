package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.import.parser.manami.ManamiVersionHandler
import io.github.manamiproject.manami.app.import.parser.manami.SemanticVersion
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.models.*
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.newInputStream
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.net.URI
import javax.xml.parsers.SAXParserFactory

internal class FileParser : Parser<ParsedManamiFile> {

    private val saxParser = SAXParserFactory.newInstance().newSAXParser()
    private val versionHandler = ManamiVersionHandler()
    private val documentHandler = ManamiFileHandler()

    override fun handlesSuffix(): FileSuffix = "xml"

    override fun parse(file: RegularFile): ParsedManamiFile {
        require(file.regularFileExists()) { "Given path [${file.toAbsolutePath()}] is either not a file or doesn't exist." }
        require(file.fileSuffix() == handlesSuffix()) { "Parser doesn't support given file suffix." }

        saxParser.parse(file.newInputStream(), versionHandler)
        require(versionHandler.version == minVersion || versionHandler.version.isNewerThan(minVersion)) { "Unable to parse manami file older than $minVersion" }

        saxParser.parse(file.newInputStream(), documentHandler)
        return documentHandler.parsedFile
    }

    private companion object {
        private val minVersion = SemanticVersion("3.0.0")
    }
}

private class ManamiFileHandler : DefaultHandler() {

    private var strBuilder = StringBuilder()
    private val animeListEntries = mutableSetOf<AnimeListEntry>()
    private val watchListEntries = mutableSetOf<WatchListEntry>()
    private val ignoreListEntries = mutableSetOf<IgnoreListEntry>()
    var parsedFile = ParsedManamiFile()

    override fun characters(ch: CharArray, start: Int, length: Int) {
        strBuilder.append(String(ch, start, length))
    }

    override fun startElement(namespaceUri: String, localName: String, qName: String, attributes: Attributes) {
        strBuilder = StringBuilder()

        when (qName) {
            "animeListEntry" -> createAnimeEntry(attributes)
            "watchListEntry" -> createWatchListEntry(attributes)
            "ignoreListEntry" -> createIgnoreListEnty(attributes)
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
                        episodes = attributes.getValue("episodes").trim().toInt(),
                        type = Anime.Type.valueOf(attributes.getValue("type").trim()),
                        location = URI(attributes.getValue("location").trim()),
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
            )
        )
    }

    private fun createIgnoreListEnty(attributes: Attributes) {
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

    override fun endDocument() {
        parsedFile = ParsedManamiFile(
                animeListEntries = animeListEntries,
                watchListEntries = watchListEntries,
                ignoreListEntries = ignoreListEntries,
        )
    }
}