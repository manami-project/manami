package io.github.manamiproject.manami.app.inconsistencies

internal interface InconsistencyHandler<out RESULT> {

    suspend fun execute(): RESULT
}