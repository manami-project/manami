package io.github.manami.persistence.inmemory.watchlist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static io.github.manami.dto.entities.MinimalEntry.isValidMinimalEntry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Named;

import com.google.common.collect.ImmutableList;

import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.WatchListHandler;

/**
 * @author manami-project
 * @since 2.7.0
 */
@Named
public class InMemoryWatchListHandler implements WatchListHandler {

    private final Map<InfoLink, WatchListEntry> watchList;


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
    public boolean watchListEntryExists(final InfoLink infoLink) {
        return watchList.containsKey(infoLink);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        if (!isValidMinimalEntry(anime) || watchList.containsKey(anime.getInfoLink())) {
            return false;
        }

        Optional<WatchListEntry> entry = Optional.empty();

        if (anime instanceof Anime || anime instanceof FilterEntry) {
            entry = WatchListEntry.valueOf(anime);
        } else if (anime instanceof WatchListEntry) {
            entry = Optional.of((WatchListEntry) anime);
        }

        if (!entry.isPresent()) {
            return false;
        }

        watchList.put(entry.get().getInfoLink(), entry.get());
        return true;
    }


    @Override
    public boolean removeFromWatchList(final InfoLink infoLink) {
        if (infoLink != null && infoLink.isValid()) {
            return watchList.remove(infoLink) != null;
        }

        return false;
    }


    /**
     * @since 2.7.0
     */
    public void clear() {
        watchList.clear();
    }


    public void updateOrCreate(final WatchListEntry entry) {
        if (isValidMinimalEntry(entry)) {
            watchList.put(entry.getInfoLink(), entry);
        }
    }
}
