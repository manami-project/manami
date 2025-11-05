package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart

internal data class SimpleTableConfig(
    var keyHeadline: String = "Key",
    var valueHeadline: String = "Value",
    var weights: List<Float> = listOf(2f, 10f),
    var headerFontSize: Float = 18f,
    var headerAlignment: Alignment = Center,
    var contentFontSize: Float = 18f,
    var contentAlignment: Alignment = CenterStart,
)
