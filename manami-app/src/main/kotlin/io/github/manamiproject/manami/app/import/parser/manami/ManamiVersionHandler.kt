package io.github.manamiproject.manami.app.import.parser.manami

import org.xml.sax.Attributes
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

internal class ManamiVersionHandler : DefaultHandler() {

    private var strBuilder = StringBuilder()

    private var _version = SemanticVersion()
    val version
        get() = _version

    var entityResolver: EntityResolver = EntityResolver { _, _ -> InputSource("") }

    override fun resolveEntity(publicId: String?, systemId: String?): InputSource = entityResolver.resolveEntity(publicId, systemId)

    override fun characters(ch: CharArray, start: Int, length: Int) {
        strBuilder.append(String(ch, start, length))
    }

    override fun startElement(namespaceUri: String, localName: String, qName: String, attributes: Attributes) {

        when (qName) {
            "manami" -> {
                strBuilder = StringBuilder()
                strBuilder.append(attributes.getValue("version").trim())
            }
        }
    }

    override fun endDocument() {
        _version = SemanticVersion(strBuilder.toString())
    }
}