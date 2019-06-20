package io.github.manami.persistence.inmemory.animelist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static io.github.manami.dto.entities.Anime.isValidAnime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Named;

import com.google.common.collect.ImmutableList;

import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.persistence.AnimeListHandler;

@Named
public class InMemoryAnimeListHandler implements AnimeListHandler {

    private final Map<UUID, Anime> animeList;


    public InMemoryAnimeListHandler() {
        animeList = newConcurrentMap();
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (!isValidAnime(anime) || isInList(anime)) {
            return false;
        }

        animeList.put(anime.getId(), anime);
        return true;
    }


    @Override
    public List<Anime> fetchAnimeList() {
        final List<Anime> sortList = newArrayList(animeList.values());
        Collections.sort(sortList, new MinimalEntryComByTitleAsc());
        return ImmutableList.copyOf(sortList);
    }


    @Override
    public boolean animeEntryExists(final InfoLink infoLink) {
        return isInList(infoLink);
    }


    private boolean isInList(final Anime anime) {
        return animeList.containsKey(anime.getId()) || isInList(anime.getInfoLink());
    }


    private boolean isInList(final InfoLink infoLink) {
        if (infoLink != null && infoLink.isValid()) {
            for (final MinimalEntry curEntry : animeList.values()) {
                if (infoLink.equals(curEntry.getInfoLink())) {
                    return true;
                }
            }
        }

        return false;
    }


    @Override
    public boolean removeAnime(final UUID id) {
        if (id == null) {
            return false;
        }

        return animeList.remove(id) != null;
    }


    public void clear() {
        animeList.clear();
    }


    public void updateOrCreate(final Anime anime) {
        if (isValidAnime(anime)) {
            animeList.put(anime.getId(), anime);
        }
    }
}
