package io.github.manami.persistence;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.dto.events.AnimeListChangedEvent;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;

/**
 * This is a facade which is used by the application to hide which strategy is
 * actually used.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
public class PersistenceFacade implements PersistenceHandler {

    /** Currently used db persistence strategy. */
    private final PersistenceHandler strategy;

    /** Event bus. */
    private final EventBus eventBus;


    /**
     * Constructor injecting the currently used strategy.
     *
     * @since 2.0.0
     * @param strategy
     *            Currently used strategy.
     */
    @Inject
    public PersistenceFacade(@Named("inMemoryStrategy") final PersistenceHandler strategy, final EventBus eventBus) {
        this.strategy = strategy;
        this.eventBus = eventBus;
    }


    @Override
    public boolean filterAnime(final MinimalEntry anime) {
        if (anime != null && isValid(anime)) {
            if (strategy.filterAnime(anime)) {
                eventBus.post(new AnimeListChangedEvent());
                return true;
            }
        }

        return false;
    }


    @Override
    public List<FilterEntry> fetchFilterList() {
        return strategy.fetchFilterList();
    }


    @Override
    public boolean filterEntryExists(final String url) {
        return strategy.filterEntryExists(url);
    }


    @Override
    public boolean removeFromFilterList(final String url) {
        if (strategy.removeFromFilterList(url)) {
            eventBus.post(new AnimeListChangedEvent());
            return true;
        }

        return false;
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (anime != null && isValid(anime)) {
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
    public boolean animeEntryExists(final String url) {
        return strategy.animeEntryExists(url);
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        return strategy.fetchWatchList();
    }


    @Override
    public boolean watchListEntryExists(final String url) {
        return strategy.watchListEntryExists(url);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        if (anime != null && isValid(anime)) {
            if (strategy.watchAnime(anime)) {
                eventBus.post(new AnimeListChangedEvent());
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean removeFromWatchList(final String url) {
        return strategy.removeFromWatchList(url);
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


    /**
     * Checks if an anime entry is valid.
     *
     * @param anime
     * @return
     */
    private boolean isValid(final Anime anime) {
        boolean ret = anime != null;
        ret &= anime.getId() != null;
        ret &= anime.getType() != null;
        ret &= anime.getEpisodes() >= 0;
        ret &= StringUtils.isNotBlank(anime.getTitle());
        ret &= StringUtils.isNotBlank(anime.getLocation());

        return ret;
    }


    /**
     * Checks if an anime entry is Valid
     *
     * @param anime
     * @return
     */
    private boolean isValid(final MinimalEntry anime) {
        boolean ret = anime != null;
        ret &= StringUtils.isNotBlank(anime.getTitle());
        ret &= StringUtils.isNotBlank(anime.getInfoLink());

        return ret;
    }


    @Override
    public void addAnimeList(final List<Anime> list) {
        strategy.addAnimeList(list);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void addFilterList(final List<? extends MinimalEntry> list) {
        strategy.addFilterList(list);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void addWatchList(final List<? extends MinimalEntry> list) {
        strategy.addWatchList(list);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void updateOrCreate(final Anime anime) {
        strategy.updateOrCreate(anime);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void updateOrCreate(final WatchListEntry entry) {
        strategy.updateOrCreate(entry);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void updateOrCreate(final FilterEntry entry) {
        strategy.updateOrCreate(entry);
        eventBus.post(new AnimeListChangedEvent());
    }


    @Override
    public void updateOrCreate(final MinimalEntry entry) {
        strategy.updateOrCreate(entry);
        eventBus.post(new AnimeListChangedEvent());
    }
}
