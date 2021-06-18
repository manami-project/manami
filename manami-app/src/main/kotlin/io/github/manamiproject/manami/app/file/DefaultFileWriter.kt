package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.versioning.ResourceBasedVersionProvider
import io.github.manamiproject.manami.app.versioning.VersionProvider
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.newOutputStream
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.loadResource
import javax.xml.stream.XMLOutputFactory

internal class DefaultFileWriter(
    private val state: State = InternalState,
    private val versionProvider: VersionProvider = ResourceBasedVersionProvider
) : FileWriter {

    private val xmlWriterFactory = XMLOutputFactory.newInstance()

    override fun writeTo(file: RegularFile) {
        val folder = file.parent
        val dtdFile = "manami_${versionProvider.version()}.dtd"

        loadResource("config/animelist.dtd").writeToFile(folder.resolve(dtdFile))

        val xmlWriter = xmlWriterFactory.createXMLStreamWriter(file.newOutputStream()).apply {
            writeStartDocument("UTF-8", "1.1")
            writeCharacters(LINEBREAK)
            writeDTD("<!DOCTYPE manami SYSTEM \"$dtdFile\">")
            writeCharacters(LINEBREAK)
            writeStartElement("manami")
            writeAttribute("version", versionProvider.version().toString())
            writeCharacters(LINEBREAK)
            writeCharacters(IDENT_1)
            writeStartElement("animeList")
            writeCharacters(LINEBREAK)
        }

        state.animeList().sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes })).forEach { animeListEntry ->
            xmlWriter.writeCharacters(IDENT_2)
            xmlWriter.writeEmptyElement("animeListEntry")
            xmlWriter.writeAttribute("link", animeListEntry.link.toString())
            xmlWriter.writeAttribute("title", animeListEntry.title)
            xmlWriter.writeAttribute("thumbnail", animeListEntry.thumbnail.toString())
            xmlWriter.writeAttribute("type", animeListEntry.type.toString())
            xmlWriter.writeAttribute("episodes", animeListEntry.episodes.toString())
            xmlWriter.writeAttribute("location", animeListEntry.location.toString())
            xmlWriter.writeCharacters(LINEBREAK)
        }

        xmlWriter.writeCharacters(IDENT_1)
        xmlWriter.writeEndElement()
        xmlWriter.writeCharacters(LINEBREAK)
        xmlWriter.writeCharacters(IDENT_1)
        xmlWriter.writeStartElement("watchList")
        xmlWriter.writeCharacters(LINEBREAK)

        state.watchList().sortedBy { it.title }.forEach { watchListEntry ->
            xmlWriter.writeCharacters(IDENT_2)
            xmlWriter.writeEmptyElement("watchListEntry")
            xmlWriter.writeAttribute("link", watchListEntry.link.toString())
            xmlWriter.writeAttribute("title", watchListEntry.title)
            xmlWriter.writeAttribute("thumbnail", watchListEntry.thumbnail.toString())
            xmlWriter.writeCharacters(LINEBREAK)
        }

        xmlWriter.writeCharacters(IDENT_1)
        xmlWriter.writeEndElement()
        xmlWriter.writeCharacters(LINEBREAK)
        xmlWriter.writeCharacters(IDENT_1)
        xmlWriter.writeStartElement("ignoreList")
        xmlWriter.writeCharacters(LINEBREAK)

        state.ignoreList().sortedBy { it.title }.forEach { ignoreListEntry ->
            xmlWriter.writeCharacters(IDENT_2)
            xmlWriter.writeEmptyElement("ignoreListEntry")
            xmlWriter.writeAttribute("link", ignoreListEntry.link.toString())
            xmlWriter.writeAttribute("title", ignoreListEntry.title)
            xmlWriter.writeAttribute("thumbnail", ignoreListEntry.thumbnail.toString())
            xmlWriter.writeCharacters(LINEBREAK)
        }

        xmlWriter.writeCharacters(IDENT_1)
        xmlWriter.writeEndElement()
        xmlWriter.writeCharacters(LINEBREAK)
        xmlWriter.writeEndElement()
        xmlWriter.writeEndDocument()
    }
    
    companion object {
        private const val IDENT_1 = "  "
        private const val IDENT_2 = "    "
        private const val LINEBREAK = "\n"
    }
}