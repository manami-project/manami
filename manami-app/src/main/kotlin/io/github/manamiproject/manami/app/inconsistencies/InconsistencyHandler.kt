package io.github.manamiproject.manami.app.inconsistencies

internal interface InconsistencyHandler<RESULT> {

    fun calculateWorkload(): Int

    fun execute(progressUpdate: (Int) -> Unit = {}): RESULT
}