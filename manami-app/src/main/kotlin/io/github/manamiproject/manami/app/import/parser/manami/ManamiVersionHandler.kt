package io.github.manamiproject.manami.app.import.parser.manami

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

internal class ManamiVersionHandler : DefaultHandler() {

    private var strBuilder = StringBuilder()
    var version = SemanticVersion()

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
        version = SemanticVersion(strBuilder.toString())
    }
}