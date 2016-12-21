package io.github.manami.persistence.inmemory;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

import static io.github.manami.dto.entities.Anime.isValidAnime;
import static io.github.manami.dto.entities.MinimalEntry.isValidMinimalEntry;

/**
 * @author manami-project
 * @since 2.7.0
 */
@Named("inMemoryStrategy")
public class InMemoryPersistenceHandler implements PersistenceHandler {

    private final InMemoryFilterListHandler filterListHandler;
    private final InMemoryAnimeListHandler animeListHandler;
    private final InMemoryWatchListHandler watchListHandler;


    /**
     * @since 2.7.0
     * @param animeListHandler
     * @param filterListHandler
     * @param watchListHandler
     */
    @Inject
    public InMemoryPersistenceHandler(final InMemoryAnimeListHandler animeListHandler, final InMemoryFilterListHandler filterListHandler, final InMemoryWatchListHandler watchListHandler) {
        this.animeListHandler = animeListHandler;
        this.filterListHandler = filterListHandler;
        this.watchListHandler = watchListHandler;
    }


    @Override
    public boolean filterAnime(final MinimalEntry anime) {
        if (isValidMinimalEntry(anime)) {
            if (anime.getInfoLink().isValid()) {
                watchListHandler.removeFromWatchList(anime.getInfoLink());
            }

            return filterListHandler.filterAnime(anime);
        }

        return false;
    }


    @Override
    public List<FilterEntry> fetchFilterList() {
        return filterListHandler.fetchFilterList();
    }


    @Override
    public boolean filterEntryExists(final InfoLink infoLink) {
        return filterListHandler.filterEntryExists(infoLink);
    }


    @Override
    public boolean removeFromFilterList(final InfoLink infoLink) {
        if (infoLink != null && infoLink.isValid()) {
            return filterListHandler.removeFromFilterList(infoLink);
        }

        return false;
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (isValidAnime(anime)) {
            if (anime.getInfoLink().isValid()) {
                filterListHandler.removeFromFilterList(anime.getInfoLink());
                watchListHandler.removeFromWatchList(anime.getInfoLink());
            }
            return animeListHandler.addAnime(anime);
        }
        return false;
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        return watchListHandler.fetchWatchList();
    }


    @Override
    public boolean watchListEntryExists(final InfoLink infoLink) {
        return watchListHandler.watchListEntryExists(infoLink);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        if (isValidMinimalEntry(anime)) {
            if (anime.getInfoLink().isValid()) {
                filterListHandler.removeFromFilterList(anime.getInfoLink());
            }
            return watchListHandler.watchAnime(anime);
        }

        return false;
    }


    @Override
    public boolean removeFromWatchList(final InfoLink infoLink) {
        if (infoLink != null && infoLink.isValid()) {
            return watchListHandler.removeFromWatchList(infoLink);
        }

        return false;
    }


    @Override
    public List<Anime> fetchAnimeList() {
        return animeListHandler.fetchAnimeList();
    }


    @Override
    public boolean animeEntryExists(final InfoLink infoLink) {
        return animeListHandler.animeEntryExists(infoLink);
    }


    @Override
    public boolean removeAnime(final UUID id) {
        return animeListHandler.removeAnime(id);
    }


    @Override
    public void clearAll() {
        animeListHandler.clear();
        watchListHandler.clear();
        filterListHandler.clear();
    }


    @Override
    public void addAnimeList(final List<Anime> list) {
        if (list != null) {
            list.forEach(animeListHandler::addAnime);
        }
    }


    @Override
    public void addFilterList(final List<? extends MinimalEntry> list) {
        if (list != null) {
            list.forEach(filterListHandler::filterAnime);
        }
    }


    @Override
    public void addWatchList(final List<? extends MinimalEntry> list) {
        if (list != null) {
            list.forEach(watchListHandler::watchAnime);
        }
    }


    @Override
    public void updateOrCreate(final MinimalEntry entry) {
        if (entry == null) {
            return;
        }

        if (entry instanceof Anime) {
            animeListHandler.updateOrCreate((Anime) entry);
        } else if (entry instanceof FilterEntry) {
            filterListHandler.updateOrCreate((FilterEntry) entry);
        } else if (entry instanceof WatchListEntry) {
            watchListHandler.updateOrCreate((WatchListEntry) entry);
        }
    }
}
