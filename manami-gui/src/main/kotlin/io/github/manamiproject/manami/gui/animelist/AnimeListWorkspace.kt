package io.github.manamiproject.manami.gui.animelist

import tornadofx.Workspace

class AnimeListWorkspace : Workspace() {

    private val animelist: Animelist by inject()

    override fun onBeforeShow() {
        add(animelist.root)
    }
}