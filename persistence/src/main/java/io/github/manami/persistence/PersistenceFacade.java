package io.github.manami.persistence;

import com.google.common.eventbus.EventBus;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.dto.events.AnimeListChangedEvent;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

import static io.github.manami.dto.entities.Anime.isValidAnime;
import static io.github.manami.dto.entities.MinimalEntry.isValidMinimalEntry;

/**
 * This is a facade which is used by the application to hide which strategy is
 * actually used.
 */
@Named
public class PersistenceFacade implements PersistenceHandler {

    /** Currently used db persistence strategy. */
    private final PersistenceHandler strategy;

    /** Event bus. */
    private final EventBus eventBus;


    /**
     * Constructor injecting the currently used strategy.
     * @param strategy Currently used strategy.
     */
    @Inject
    public PersistenceFacade(@Named("inMemoryStrategy") final PersistenceHandler strategy, final EventBus eventBus) {
        this.strategy = strategy;
        this.eventBus = eventBus;
    }


    @Override
    public boolean filterAnime(final MinimalEntry anime) {
        if (isValidMinimalEntry(anime)) {
            if (strategy.filterAnime(anime)) {
                eventBus.post(new AnimeListChangedEvent());
                return true;
            }
        }

        return false;
    }


    @Override
    public List<FilterListEntry> fetchFilterList() {
        return strategy.fetchFilterList();
    }


    @Override
    public boolean filterEntryExists(final InfoLink infoLink) {
        return strategy.filterEntryExists(infoLink);
    }


    @Override
    public boolean removeFromFilterList(final InfoLink infoLink) {
        if (infoLink == null || !infoLink.isValid()) {
            return false;
        }

        if (strategy.removeFromFilterList(infoLink)) {
            eventBus.post(new AnimeListChangedEvent());
            return true;
        }

        return false;
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (isValidAnime(anime)) {
            if (strategy.addAnime(anime)) {
                eventBus.post(new AnimeListChangedEvent());
                return true;
            }
        }

        return false;
    }


    @Override
    public List<Anime> fetchAnimeList() {
        return strategy.fetchAnimeList();
    }


    @Override
    public boolean animeEntryExists(final InfoLink infoLink) {
        return strategy.animeEntryExists(infoLink);
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        return strategy.fetchWatchList();
    }


    @Override
    public boolean watchListEntryExists(final InfoLink infoLink) {
        return strategy.watchListEntryExists(infoLink);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        if (isValidMinimalEntry(anime)) {
            if (strategy.watchAnime(anime)) {
                eventBus.post(new AnimeListChangedEvent());
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean removeFromWatchList(final InfoLink infoLink) {
        if (infoLink ==null || !infoLink.isValid()) {
            return false;
        }

        if (strategy.removeFromWatchList(infoLink)) {
            eventBus.post(new AnimeListChangedEvent());
            return true;
        }

        return false;
    }


    @Override
    public boolean removeAnime(final UUID id) {
        if (strategy.removeAnime(id)) {
            eventBus.post(new AnimeListChangedEvent());
            return true;
        }

        return false;
    }


    @Override
    public void clearAll() {
        strategy.clearAll();
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void addAnimeList(final List<Anime> list) {
        if (list != null) {
            strategy.addAnimeList(list);
            eventBus.post(new AnimeListChangedEvent());
        }
    }


    @Override
    public void addFilterList(final List<? extends MinimalEntry> list) {
        if (list != null) {
            strategy.addFilterList(list);
            eventBus.post(new AnimeListChangedEvent());
        }
    }


    @Override
    public void addWatchList(final List<? extends MinimalEntry> list) {
        if (list != null) {
            strategy.addWatchList(list);
            eventBus.post(new AnimeListChangedEvent());
        }
    }


    @Override
    public void updateOrCreate(final MinimalEntry entry) {
        if (entry != null) {
            strategy.updateOrCreate(entry);
            eventBus.post(new AnimeListChangedEvent());
        }
    }
}
