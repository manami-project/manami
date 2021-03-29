package io.github.manamiproject.manami.gui.components

import eu.hansolo.tilesfx.Tile
import eu.hansolo.tilesfx.Tile.SkinType.TEXT
import eu.hansolo.tilesfx.Tile.TextSize.BIGGER
import eu.hansolo.tilesfx.TileBuilder
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos.CENTER
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.add

data class NumberTileConfig(
    var title: String = EMPTY,
    var color: Color = Color.LIGHTGRAY,
    var valueProperty: SimpleStringProperty = SimpleStringProperty(EMPTY),
)

inline fun EventTarget.numberTile(config: NumberTileConfig.() -> Unit): Tile {
    val numberTileConfig = NumberTileConfig().apply(config)

    val tile = TileBuilder.create()
        .skinType(TEXT)
        .backgroundColor(numberTileConfig.color)
        .maxSize(400.0, 150.0)
        .text(numberTileConfig.title)
        .description(numberTileConfig.valueProperty.get())
        .descriptionAlignment(CENTER)
        .textSize(BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .textVisible(true)
        .build()

    numberTileConfig.valueProperty.addListener { _, _, newValue ->
        tile.description = newValue
    }

    this.add(tile)

    return tile
}