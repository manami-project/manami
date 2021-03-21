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
    val progressIndicatorVisibleProperty: SimpleBooleanProperty = SimpleBooleanProperty(false),
    val progressIndicatorValueProperty: SimpleDoubleProperty = SimpleDoubleProperty(0.0),
    var finishedTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    var numberOfTasksProperty: SimpleIntegerProperty = SimpleIntegerProperty(0),
    val startButtonDisableProperty: SimpleBooleanProperty = SimpleBooleanProperty(false),
    var onStart: () -> Unit = {},
)

inline fun EventTarget.simpleServiceStart(config: SimpleServiceStartConfig.() -> Unit): HBox {
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
    }

    return hbox {
        hgrow = ALWAYS
        alignment = CENTER
        padding = Insets(10.0)
        spacing = 5.0

        button {
            text = "Start"
            isDefaultButton = true
            disableProperty().bindBidirectional(simpleServiceStartConfig.startButtonDisableProperty)
            action {
                simpleServiceStartConfig.startButtonDisableProperty.set(true)
                simpleServiceStartConfig.progressIndicatorVisibleProperty.set(true)
                simpleServiceStartConfig.progressIndicatorValueProperty.set(-1.0)
                runAsync { simpleServiceStartConfig.onStart.invoke() }
            }
        }

        progressindicator {
            progressProperty().bindBidirectional(simpleServiceStartConfig.progressIndicatorValueProperty)
            visibleProperty().bindBidirectional(simpleServiceStartConfig.progressIndicatorVisibleProperty)
        }
    }
}