package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.state.events.AnimeListChangedEvent
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import tornadofx.Controller
import tornadofx.FXEvent

class ManamiAccess(private val manami: ManamiApp = Manami()) : Controller(), ManamiApp by manami {

    init {
        (manami as Manami).eventMapping {
            fire(
                when(this) {
                    AnimeListChangedEvent -> AnimeListChangedFxEvent
                    else -> throw IllegalStateException("Unmapped event: [${this::class.simpleName}]")
                }
            )
        }
    }
}

sealed class GuiEvent : FXEvent()
object AnimeListChangedFxEvent : GuiEvent()