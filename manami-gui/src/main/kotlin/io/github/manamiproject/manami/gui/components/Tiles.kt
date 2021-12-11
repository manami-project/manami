package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.geometry.Pos.TOP_CENTER
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import tornadofx.*

data class NumberTileConfig(
    var title: String = EMPTY,
    var color: Color = Color.LIGHTGRAY,
    var valueProperty: SimpleStringProperty = SimpleStringProperty(EMPTY),
)

inline fun EventTarget.numberTile(config: NumberTileConfig.() -> Unit): Node {
    val numberTileConfig = NumberTileConfig().apply(config)

    val tile = Rectangle(0.0, 0.0, 250.0, 150.0).apply {
        fill = numberTileConfig.color
    }

    val content = text {
        text = "0"
        fill = Color.WHITE
        font = Font.font(16.0)
    }

    val innerPane = pane {
        prefWidth = 250.0
        prefHeight = 150.0

        vbox {
            padding = insets(5)
            fitToParentSize()
            hbox {
                alignment = TOP_CENTER
                text {
                    text = numberTileConfig.title
                    fill = Color.WHITE
                }
            }
            hbox {
                fitToParentSize()
                alignment = Pos.CENTER
                add(content)
            }
        }
    }

    numberTileConfig.valueProperty.addListener { _, _, newValue ->
        content.text = newValue
    }

    val group = Group(tile, innerPane)

    this.add(group)

    return group
}