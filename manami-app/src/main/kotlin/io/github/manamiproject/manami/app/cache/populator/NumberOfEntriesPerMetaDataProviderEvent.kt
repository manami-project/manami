package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.modb.core.config.Hostname

data class NumberOfEntriesPerMetaDataProviderEvent(val entries: Map<Hostname, Int>): Event