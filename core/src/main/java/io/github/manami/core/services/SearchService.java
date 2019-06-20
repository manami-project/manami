package io.github.manami.core.services;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import com.google.common.eventbus.EventBus;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.dto.events.SearchResultEvent;
import io.github.manami.persistence.PersistenceHandler;

public class SearchService extends AbstractService<Void> {

    private final PersistenceHandler persistanceHandler;
    private final SearchResultEvent event;
    private final EventBus eventBus;
    private final String searchString;


    public SearchService(final String searchString, final PersistenceHandler persistanceHandler, final EventBus eventBus) {
        this.searchString = searchString;
        this.persistanceHandler = persistanceHandler;
        this.eventBus = eventBus;
        event = new SearchResultEvent(searchString);
    }


    @Override
    protected Void execute() {
        persistanceHandler.fetchAnimeList().forEach(this::checkEntry);
        persistanceHandler.fetchFilterList().forEach(this::checkEntry);
        persistanceHandler.fetchWatchList().forEach(this::checkEntry);

        eventBus.post(event);

        return null;
    }


    private void checkEntry(final MinimalEntry entry) {
        final boolean isTitleNearlyEqual = getLevenshteinDistance(entry.getTitle().toLowerCase(), searchString.toLowerCase()) <= 2;
        final boolean isInTitle = containsIgnoreCase(entry.getTitle(), searchString);
        final boolean isInfoLinkEqual = searchString.equalsIgnoreCase(entry.getInfoLink().getUrl());

        if (isTitleNearlyEqual || isInTitle || isInfoLinkEqual) {
            addToList(entry);
        }
    }


    private void addToList(final MinimalEntry entry) {
        if (entry instanceof Anime) {
            event.addAnimeListSearchResult(entry);
        } else if (entry instanceof FilterListEntry) {
            event.addFilterListSearchResult(entry);
        } else if (entry instanceof WatchListEntry) {
            event.addWatchListSearchResult(entry);
        }
    }
}
