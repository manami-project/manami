package io.github.manami.persistence.inmemory.watchlist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.common.collect.ImmutableList;

import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.WatchListHandler;

/**
 * @author manami-project
 * @since 2.7.0
 */
@Named
public class InMemoryWatchListHandler implements WatchListHandler {

    private final Map<String, WatchListEntry> watchList;


    public InMemoryWatchListHandler() {
        watchList = newConcurrentMap();
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        final List<WatchListEntry> sortList = newArrayList(watchList.values());
        Collections.sort(sortList, new MinimalEntryComByTitleAsc());
        return ImmutableList.copyOf(sortList);
    }


    @Override
    public boolean watchListEntryExists(final String url) {
        return watchList.containsKey(url);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        if (anime == null || isBlank(anime.getInfoLink()) || watchList.containsKey(anime.getInfoLink())) {
            return false;
        }

        WatchListEntry entry = null;

        if (anime instanceof Anime || anime instanceof FilterEntry) {
            entry = WatchListEntry.valueOf(anime);
        } else if (anime instanceof WatchListEntry) {
            entry = (WatchListEntry) anime;
        }

        if (entry != null && isBlank(entry.getThumbnail())) {
            entry.setThumbnail(AbstractMinimalEntry.NO_IMG_THUMB);
        }

        if (entry != null) {
            watchList.put(entry.getInfoLink(), entry);
            return true;
        }

        return false;
    }


    @Override
    public boolean removeFromWatchList(final String url) {
        return watchList.remove(url) != null;
    }


    /**
     * @since 2.7.0
     */
    public void clear() {
        watchList.clear();
    }


    public void updateOrCreate(final WatchListEntry entry) {
        if (entry != null && isNotBlank(entry.getInfoLink())) {
            watchList.put(entry.getInfoLink(), entry);
        }
    }
}
