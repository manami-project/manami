package io.github.manamiproject.manami.app.inconsistencies

interface InconsistenciesHandler {

    fun findInconsistencies(config: InconsistenciesSearchConfig)

    fun fixMetaDataInconsistencies()

    fun fixDeadEntryInconsistencies()
}