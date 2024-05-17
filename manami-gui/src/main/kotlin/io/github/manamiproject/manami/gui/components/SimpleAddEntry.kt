package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
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
    var finishedTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var numberOfTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var onAdd: (Collection<URI>) -> Unit = {},
)

inline fun EventTarget.simpleAnimeAddition(config: SimpleAnimeAdditionConfig.() -> Unit): HBox {
    val progressIndicatorVisibleProperty = SimpleBooleanProperty(false)
    val progressIndicatorValueProperty = SimpleDoubleProperty(0.0)

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

    val progressIndicatorWidthProperty = SimpleDoubleProperty(25.0)
    progressIndicatorValueProperty.addListener { _, _, newValue ->
        when {
            newValue as Double >= 0.0 -> progressIndicatorWidthProperty.set(50.0)
            else -> progressIndicatorWidthProperty.set(25.0)
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
            minWidth = 260.0
        }

        button {
            text = "add"
            isDefaultButton = true
            action {
                if (txtUrls.text.eitherNullOrBlank()) {
                    txtUrls.text = EMPTY
                }

                val urls = txtUrls.text.trim().split(' ').map { it.trim() }.map { URI(it) }

                if (urls.isNotEmpty()) {
                    progressIndicatorValueProperty.set(-1.0)
                    progressIndicatorVisibleProperty.set(true)
                    runAsync { simpleAnimeAdditionConfig.onAdd.invoke(urls) }
                    txtUrls.text = EMPTY
                }
            }
        }

        progressindicator {
            progressProperty().bindBidirectional(progressIndicatorValueProperty)
            visibleProperty().bindBidirectional(progressIndicatorVisibleProperty)
            maxWidthProperty().bindBidirectional(progressIndicatorWidthProperty)
            maxHeightProperty().unbind()
            maxHeightProperty().bindBidirectional(maxWidthProperty())
            maxWidthProperty().set(25.0)
        }
    }
}