package io.github.manamiproject.manami.app.import.parser.manami

import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.models.NoLink
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.newInputStream
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.net.URI
import java.net.URL
import javax.xml.parsers.SAXParserFactory

internal class ManamiLegacyFileParser : Parser {

    private val saxParser = SAXParserFactory.newInstance().newSAXParser()
    private val versionHandler = ManamiVersionHandler()
    private val documentHandler = ManamiLegacyFileHandler()

    override fun handlesSuffix(): FileSuffix = "xml"

    override fun parse(file: RegularFile): ParsedFile {
        require(file.regularFileExists()) { "Given path [${file.toAbsolutePath()}] is either not a file or doesn't exist." }
        require(file.fileSuffix() == handlesSuffix()) { "Parser doesn't support given file suffix." }

        saxParser.parse(file.newInputStream(), versionHandler)
        require(versionHandler.version != maxVersion && versionHandler.version.isOlderThan(maxVersion)) { "Unable to parse manami file newer than $maxVersion" }

        saxParser.parse(file.newInputStream(), documentHandler)
        return documentHandler.parsedFile
    }

    private companion object {
        private val maxVersion = SemanticVersion("3.0.0")
    }
}

private class ManamiLegacyFileHandler : DefaultHandler() {

    private var strBuilder = StringBuilder()
    private val animeListEntries = mutableSetOf<AnimeListEntry>()
    private val watchListEntries = mutableSetOf<URL>()
    private val ignoreListEntries = mutableSetOf<URL>()
    var parsedFile = ParsedFile()

    override fun characters(ch: CharArray, start: Int, length: Int) {
        strBuilder.append(String(ch, start, length))
    }

    override fun startElement(namespaceUri: String, localName: String, qName: String, attributes: Attributes) {
        strBuilder = StringBuilder()

        when (qName) {
            "anime" -> createAnimeEntry(attributes)
            "watchListEntry" -> watchListEntries.add(URL(attributes.getValue("infoLink").trim()))
            "filterEntry" -> ignoreListEntries.add(URL(attributes.getValue("infoLink").trim()))
        }
    }

    private fun createAnimeEntry(attributes: Attributes) {
        val link = attributes.getValue("infoLink").trim().let {
            if (it.isBlank()) {
                NoLink
            } else {
                Link(URI(it))
            }
        }

        val type = attributes.getValue("type").trim().let {
            if (it.equals("music", ignoreCase = true)) {
                Anime.Type.Special
            } else {
                Anime.Type.valueOf(it)
            }
        }

        animeListEntries.add(
            AnimeListEntry(
                link = link,
                title = attributes.getValue("title").trim(),
                episodes = attributes.getValue("episodes").trim().toInt(),
                type = type,
                location = URI(attributes.getValue("location").trim()),
            )
        )
    }

    override fun endDocument() {
        parsedFile = ParsedFile(
                animeListEntries = animeListEntries,
                watchListEntries = watchListEntries,
                ignoreListEntries = ignoreListEntries,
        )
    }
}