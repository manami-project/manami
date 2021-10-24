package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestInconsistencyHandler: InconsistencyHandler<String> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(progressUpdate: (Int) -> Unit): String = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestMetaDataInconsistencyHandler: InconsistencyHandler<MetaDataInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestDeadEntriesInconsistencyHandler: InconsistencyHandler<DeadEntriesInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}