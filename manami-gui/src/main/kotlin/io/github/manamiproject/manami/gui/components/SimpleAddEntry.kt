package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.text.FontWeight.EXTRA_BOLD
import tornadofx.*
import java.net.URI

data class SimpleAnimeAdditionConfig(
    val progressIndicatorVisibleProperty: SimpleBooleanProperty = SimpleBooleanProperty(false),
    val progressIndicatorValueProperty: SimpleDoubleProperty = SimpleDoubleProperty(0.0),
    var finishedTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var numberOfTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var onAdd: (Collection<URI>) -> Unit = {},
)

inline fun EventTarget.simpleAnimeAddition(config: SimpleAnimeAdditionConfig.() -> Unit): HBox {
    val simpleAnimeAdditionConfig = SimpleAnimeAdditionConfig().apply(config).apply {
        finishedTasksProperty.addListener { _, _, newValue ->
            val progressInPercent = if (newValue.toDouble() == 0.0) {
                0.0
            } else {
                newValue.toDouble()/numberOfTasksProperty.get().toDouble()
            }
            progressIndicatorValueProperty.set(progressInPercent)
            progressIndicatorVisibleProperty.set(numberOfTasksProperty.get() > newValue.toInt())
        }
        numberOfTasksProperty.addListener { _, _, newValue ->
            val progressInPercent = if (finishedTasksProperty.get().toDouble() == 0.0) {
                0.0
            } else {
                finishedTasksProperty.get().toDouble() / newValue.toDouble()
            }
            progressIndicatorValueProperty.set(progressInPercent)
            progressIndicatorVisibleProperty.set(finishedTasksProperty.get() < newValue.toInt())
        }
    }

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

        val txtUrls = textfield {
            promptText = "https://myanimelist.net/anime/1535 https://..."
            prefWidth = 200.0
        }

        button {
            text = "add"
            isDefaultButton = true
            action {
                if (txtUrls.text.isBlank()) {
                    txtUrls.text = EMPTY
                }

                val urls = txtUrls.text.trim().split(' ').map { it.trim() }.map { URI(it) }

                if (urls.isNotEmpty()) {
                    simpleAnimeAdditionConfig.progressIndicatorValueProperty.set(-1.0)
                    simpleAnimeAdditionConfig.progressIndicatorVisibleProperty.set(true)
                    runAsync { simpleAnimeAdditionConfig.onAdd.invoke(urls) }
                    txtUrls.text = EMPTY
                }
            }
        }

        progressindicator {
            progressProperty().bindBidirectional(simpleAnimeAdditionConfig.progressIndicatorValueProperty)
            visibleProperty().bindBidirectional(simpleAnimeAdditionConfig.progressIndicatorVisibleProperty)
        }
    }
}