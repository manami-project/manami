package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.text.FontWeight.EXTRA_BOLD
import tornadofx.*
import java.net.URI

data class SimpleAnimeAdditionConfig(
    var onAdd: (URI) -> Unit = {},
)

inline fun EventTarget.simpleAnimeAddition(config: SimpleAnimeAdditionConfig.() -> Unit): HBox {
    val simpleAnimeAdditionConfig = SimpleAnimeAdditionConfig().apply(config)

    return hbox {
        hgrow = ALWAYS
        alignment = CENTER
        padding = Insets(10.0)
        spacing = 5.0

        label {
            text = "URL"
            style {
                fontWeight = EXTRA_BOLD
            }
        }

        val txtUrl = textfield {
            promptText = "https://myanimelist.net/anime/1535"
            prefWidth = 200.0
        }

        button {
            text = "add"
            isDefaultButton = true
            action {
                val uri = URI(txtUrl.text)
                runAsync { simpleAnimeAdditionConfig.onAdd.invoke(uri) }
                txtUrl.text = EMPTY
            }
        }
    }
}