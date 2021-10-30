package io.github.manamiproject.manami.app.commands

internal interface Command {

    /**
     * @return **true** if could be executed successfully
     */
    fun execute(): Boolean
}