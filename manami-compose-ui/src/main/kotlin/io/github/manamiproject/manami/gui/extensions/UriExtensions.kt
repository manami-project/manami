package io.github.manamiproject.manami.gui.extensions

import java.awt.Desktop
import java.net.URI

fun URI.toOnClick(): () -> Unit = {
    try {
        Desktop.getDesktop().browse(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}