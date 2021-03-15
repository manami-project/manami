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
    var totalNumberOfTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var onAdd: (Collection<URI>) -> Unit = {},
)

inline fun EventTarget.simpleAnimeAddition(config: SimpleAnimeAdditionConfig.() -> Unit): HBox {
    val simpleAnimeAdditionConfig = SimpleAnimeAdditionConfig().apply(config)
    simpleAnimeAdditionConfig.finishedTasksProperty.addListener { _, _, newValue ->
        val progressInPercent = if (newValue.toDouble() == 0.0) {
            0.0
        } else {
            newValue.toDouble()/simpleAnimeAdditionConfig.totalNumberOfTasksProperty.get().toDouble()
        }
        simpleAnimeAdditionConfig.progressIndicatorValueProperty.set(progressInPercent)
        simpleAnimeAdditionConfig.progressIndicatorVisibleProperty.set(simpleAnimeAdditionConfig.totalNumberOfTasksProperty.get() > newValue.toInt())
    }
    simpleAnimeAdditionConfig.totalNumberOfTasksProperty.addListener { _, _, newValue ->
        val progressInPercent = if (simpleAnimeAdditionConfig.finishedTasksProperty.get().toDouble() == 0.0) {
            0.0
        } else {
            simpleAnimeAdditionConfig.finishedTasksProperty.get().toDouble() / newValue.toDouble()
        }
        simpleAnimeAdditionConfig.progressIndicatorValueProperty.set(progressInPercent)
        simpleAnimeAdditionConfig.progressIndicatorVisibleProperty.set(simpleAnimeAdditionConfig.finishedTasksProperty.get() < newValue.toInt())
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