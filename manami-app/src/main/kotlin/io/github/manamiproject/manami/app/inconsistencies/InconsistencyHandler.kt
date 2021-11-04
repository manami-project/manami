package io.github.manamiproject.manami.app.inconsistencies

internal interface InconsistencyHandler<RESULT> {

    fun isExecutable(config: InconsistenciesSearchConfig): Boolean

    fun calculateWorkload(): Int

    fun execute(progressUpdate: (Int) -> Unit = {}): RESULT
}