package io.github.manamiproject.manami.app.inconsistencies

internal interface InconsistencyHandler<RESULT> {

    fun isExecutable(config: InconsistenciesSearchConfig): Boolean

    fun calculateWorkload(): Int

    suspend fun execute(): RESULT
}