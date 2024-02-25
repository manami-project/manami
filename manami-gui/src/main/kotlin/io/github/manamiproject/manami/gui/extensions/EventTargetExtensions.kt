package io.github.manamiproject.manami.gui.extensions

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.*
import javafx.application.HostServices
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Hyperlink
import javafx.scene.input.MouseButton
import javafx.scene.text.Font
import java.net.URI

data class HyperlinkConfig(
    var title: String = EMPTY,
    var uri: URI = URI(EMPTY),
    var font: Font = Font.font(20.0),
    var isDisable: Boolean = false,
    var animeStatus: Anime.Status = UNKNOWN,
    var hostServicesInstance: HostServices? = null,
)

fun EventTarget.hyperlink(config: HyperlinkConfig.() -> Unit): Hyperlink {
    val hyperlinkConfig = HyperlinkConfig().apply(config)
    val title = when (hyperlinkConfig.animeStatus) {
        ONGOING -> "[ongoing]  ${hyperlinkConfig.title}"
        UPCOMING -> "[upcoming]  ${hyperlinkConfig.title}"
        else -> hyperlinkConfig.title
    }

    return Hyperlink(title).apply {
        onMouseClicked = EventHandler {
            if (it.button == MouseButton.PRIMARY) {
                hyperlinkConfig.hostServicesInstance!!.showDocument(hyperlinkConfig.uri.toString())
            }
        }
        font = hyperlinkConfig.font
        isDisable = hyperlinkConfig.isDisable
    }
}