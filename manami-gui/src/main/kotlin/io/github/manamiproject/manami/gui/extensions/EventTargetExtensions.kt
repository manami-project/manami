package io.github.manamiproject.manami.gui.extensions

import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Hyperlink
import javafx.scene.input.MouseButton
import javafx.scene.text.Font
import java.awt.Desktop
import java.net.URI

data class HyperlinkConfig(
    var title: String = EMPTY,
    var uri: URI = URI(""),
    var font: Font = Font.font(20.0),
    var isDisable: Boolean = false
)

fun EventTarget.hyperlink(config: HyperlinkConfig.() -> Unit): Hyperlink {
    val hyperlinkConfig = HyperlinkConfig().apply(config)

    return Hyperlink(hyperlinkConfig.title).apply {
        onMouseClicked = EventHandler {
            if (it.button == MouseButton.PRIMARY) {
                Desktop.getDesktop().browse(hyperlinkConfig.uri)
            }
        }
        font = hyperlinkConfig.font
        isDisable = hyperlinkConfig.isDisable
    }
}