package io.github.manamiproject.manami.gui.extensions

internal fun String.capitalize(): String {
    if (isEmpty()) return this

    val strBuilder = StringBuilder(this[0].uppercase())

    if (length > 1) {
        for (index in 1..<length) {
            strBuilder.append(this[index].lowercase())
        }
    }

    return strBuilder.toString()
}