package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestMetaDataInconsistencyHandler: InconsistencyHandler<MetaDataInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = shouldNotBeInvoked()
}

internal object TestDeadEntriesInconsistencyHandler: InconsistencyHandler<DeadEntriesInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = shouldNotBeInvoked()
}