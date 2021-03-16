package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.modb.core.extensions.EMPTY
import java.net.URI

sealed class LinkEntry

object NoLink: LinkEntry() {
    override fun toString(): String  = EMPTY
}

data class Link(val uri: URI): LinkEntry() {

    constructor(uri: String): this(
        uri = URI(uri)
    )

    override fun toString(): String = uri.toString()
}