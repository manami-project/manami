package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.foundation.lazy.LazyListState

internal class DefaultSimpleTableViewModel {

    private var lastIndex = 0
    private var lastOffset = 0
    val listState = LazyListState()

    fun saveScrollPosition() {
        lastIndex = listState.firstVisibleItemIndex
        lastOffset = listState.firstVisibleItemScrollOffset
    }

    suspend fun restoreScrollPosition() {
        listState.scrollToItem(lastIndex, lastOffset)
    }

    internal companion object {
        /**
         * Singleton of [DefaultSimpleTableViewModel]
         * @since 4.0.0
         */
        val instance: DefaultSimpleTableViewModel by lazy { DefaultSimpleTableViewModel() }
    }
}
