package io.github.manamiproject.manami.app.models

import java.net.URI

sealed class LinkEntry
object NoLink: LinkEntry()

data class Link(val uri: URI): LinkEntry() {

    constructor(uri: String): this(
        uri = URI(uri)
    )
}