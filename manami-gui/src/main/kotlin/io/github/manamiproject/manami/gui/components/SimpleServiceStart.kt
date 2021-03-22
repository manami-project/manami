package io.github.manamiproject.manami.gui.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

data class SimpleServiceStartConfig(
    var progressIndicatorVisibleProperty: SimpleBooleanProperty = SimpleBooleanProperty(false),
    var finishedTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var numberOfTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var onStart: () -> Unit = {},
)

/**
 * Termination can take place by having the same value > 0 for [SimpleServiceStartConfig.finishedTasksProperty]
 * and [SimpleServiceStartConfig.numberOfTasksProperty].
 * The other possibility is to just set [SimpleServiceStartConfig.progressIndicatorVisibleProperty] to false.
 */
inline fun EventTarget.simpleServiceStart(config: SimpleServiceStartConfig.() -> Unit): HBox {
    val progressIndicatorValueProperty = SimpleDoubleProperty(0.0)
    val startButtonDisableProperty = SimpleBooleanProperty(false)

    val simpleServiceStartConfig = SimpleServiceStartConfig().apply(config).apply {
        finishedTasksProperty.addListener { _, _, newValue ->
            val progressInPercent = if (newValue.toDouble() == 0.0) {
                0.0
            } else {
                newValue.toDouble()/numberOfTasksProperty.get().toDouble()
            }
            progressIndicatorValueProperty.set(progressInPercent)
            val showProgressIndicator = numberOfTasksProperty.get() > newValue.toInt()
            progressIndicatorVisibleProperty.set(showProgressIndicator)
            startButtonDisableProperty.set(showProgressIndicator)
        }

        numberOfTasksProperty.addListener { _, _, newValue ->
            val progressInPercent = if (finishedTasksProperty.get().toDouble() == 0.0) {
                0.0
            } else {
                finishedTasksProperty.get().toDouble() / newValue.toDouble()
            }
            progressIndicatorValueProperty.set(progressInPercent)
            val showProgressIndicator = finishedTasksProperty.get() < newValue.toInt()
            progressIndicatorVisibleProperty.set(showProgressIndicator)
            startButtonDisableProperty.set(showProgressIndicator)
        }

        progressIndicatorVisibleProperty.addListener { _, oldValue, newValue ->
            if (oldValue && !newValue && startButtonDisableProperty.get()) {
                startButtonDisableProperty.set(false)
            }
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

        button {
            text = "Start"
            isDefaultButton = true
            disableProperty().bindBidirectional(startButtonDisableProperty)
            action {
                startButtonDisableProperty.set(true)
                progressIndicatorValueProperty.set(-1.0)
                simpleServiceStartConfig.progressIndicatorVisibleProperty.set(true)
                runAsync { simpleServiceStartConfig.onStart.invoke() }
            }
        }

        progressindicator {
            progressProperty().bindBidirectional(progressIndicatorValueProperty)
            visibleProperty().bindBidirectional(simpleServiceStartConfig.progressIndicatorVisibleProperty)
            maxWidthProperty().bindBidirectional(progressIndicatorWidthProperty)
            maxHeightProperty().unbind()
            maxHeightProperty().bindBidirectional(maxWidthProperty())
            maxWidthProperty().set(25.0)
        }
    }
}