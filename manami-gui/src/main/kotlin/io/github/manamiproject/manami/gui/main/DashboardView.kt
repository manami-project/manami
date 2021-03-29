package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.numberTile
import io.github.manamiproject.modb.core.config.Hostname
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos.CENTER
import javafx.scene.paint.Color.*
import tornadofx.View
import tornadofx.gridpane
import tornadofx.row
import kotlin.collections.Collection
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.groupBy
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.math.roundToInt

class DashboardView: View() {

    private val animeListEntriesProperty = SimpleStringProperty("0")
    private val watchListEntriesProperty = SimpleStringProperty("0")
    private val ignoreListEntriesProperty = SimpleStringProperty("0")

    private val metaDataProviderEntriesInLists = mutableMapOf<Hostname, Int>()
    private val metaDataProviderTotalNumberOfEntries = mutableMapOf<Hostname, Int>()
    private val metaDataProviderSeenStringProperties = mutableMapOf<Hostname, SimpleStringProperty>().apply {
        put("myanimelist.net", SimpleStringProperty("0"))
        put("kitsu.io", SimpleStringProperty("0"))
        put("anime-planet.com", SimpleStringProperty("0"))
        put("notify.moe", SimpleStringProperty("0"))
        put("anilist.co", SimpleStringProperty("0"))
        put("anidb.net", SimpleStringProperty("0"))
    }

    init {
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            val newValue = animeListEntriesProperty.get().toInt() + event.entries.size
            animeListEntriesProperty.set(newValue.toString())

            addNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<RemoveAnimeListEntryGuiEvent> { event ->
            val newValue = animeListEntriesProperty.get().toInt() - event.entries.size
            animeListEntriesProperty.set(newValue.toString())

            removeNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<AddWatchListEntryGuiEvent> { event ->
            val newValue = watchListEntriesProperty.get().toInt() + event.entries.size
            watchListEntriesProperty.set(newValue.toString())

            addNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<RemoveWatchListEntryGuiEvent> { event ->
            val newValue = watchListEntriesProperty.get().toInt() - event.entries.size
            watchListEntriesProperty.set(newValue.toString())

            removeNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<AddIgnoreListEntryGuiEvent> { event ->
            val newValue = ignoreListEntriesProperty.get().toInt() + event.entries.size
            ignoreListEntriesProperty.set(newValue.toString())

            addNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<RemoveIgnoreListEntryGuiEvent> { event ->
            val newValue = ignoreListEntriesProperty.get().toInt() - event.entries.size
            ignoreListEntriesProperty.set(newValue.toString())

            removeNumberOfEntriesToPercentageTile(event.entries)
        }
        subscribe<NumberOfEntriesPerMetaDataProviderGuiEvent> { event ->
            metaDataProviderTotalNumberOfEntries.putAll(event.entries)
            updateStringProperties()
        }
    }

    override val root = gridpane {
        hgap = 50.0
        vgap = 50.0
        alignment = CENTER

        row {
            alignment = CENTER

            numberTile {
                title = "AnimeList"
                color = MEDIUMSEAGREEN
                valueProperty = animeListEntriesProperty
            }
            numberTile {
                title = "WatchList"
                color = CORNFLOWERBLUE
                valueProperty = watchListEntriesProperty
            }
            numberTile {
                title = "IgnoreList"
                color = INDIANRED
                valueProperty = ignoreListEntriesProperty
            }
        }
        row {
            numberTile {
                title = "myanimelist.net"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["myanimelist.net"]!!
            }
            numberTile {
                title = "kitsu.io"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["kitsu.io"]!!
            }
            numberTile {
                title = "anime-planet.com"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["anime-planet.com"]!!
            }
        }
        row {
            numberTile {
                title = "notify.moe"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["notify.moe"]!!
            }
            numberTile {
                title = "anilist.co"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["anilist.co"]!!
            }
            numberTile {
                title = "anidb.net"
                color = SLATEGRAY
                valueProperty = metaDataProviderSeenStringProperties["anidb.net"]!!
            }
        }
    }

    @Synchronized
    private fun addNumberOfEntriesToPercentageTile(entries: Collection<AnimeEntry>) {
        entries.filter { it.link is Link }.groupBy { it.link.asLink().uri.host }.forEach { (key, value) ->
            metaDataProviderEntriesInLists[key] = (metaDataProviderEntriesInLists[key] ?: 0) + value.size
        }
        updateStringProperties()
    }

    @Synchronized
    private fun removeNumberOfEntriesToPercentageTile(entries: Collection<AnimeEntry>) {
        entries.filter { it.link is Link }.groupBy { it.link.asLink().uri.host }.forEach { (key, value) ->
            metaDataProviderEntriesInLists[key] = (metaDataProviderEntriesInLists[key] ?: 0) - value.size
        }
        updateStringProperties()
    }

    @Synchronized
    private fun updateStringProperties() {
        metaDataProviderSeenStringProperties.keys.forEach {
            val inList = metaDataProviderEntriesInLists[it] ?: 0
            val totalNumber = metaDataProviderTotalNumberOfEntries[it] ?: 0

            val progressValue = when {
                inList == 0 && totalNumber == 0 -> "0"
                inList == 0 && totalNumber > 0 -> "$totalNumber"
                inList > 0 && totalNumber == 0 -> "$inList / ?"
                else -> {
                    val difference = totalNumber - inList
                    val percent = (inList.toDouble() / totalNumber.toDouble() * 100.0).roundToInt()
                    "$totalNumber - $inList = $difference ($percent%)"
                }
            }

            metaDataProviderSeenStringProperties[it]!!.set(progressValue)
        }
    }
}