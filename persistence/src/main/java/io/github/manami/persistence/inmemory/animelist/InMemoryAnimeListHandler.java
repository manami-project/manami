package io.github.manami.persistence.inmemory.animelist;

import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.persistence.AnimeListHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author manami project
 * @since 2.7.0
 */
@Named
public class InMemoryAnimeListHandler implements AnimeListHandler {

    private final Map<UUID, Anime> animeList;


    public InMemoryAnimeListHandler() {
        animeList = Maps.newConcurrentMap();
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (isInList(anime)) {
            return false;
        }

        animeList.put(anime.getId(), anime);
        return true;
    }


    @Override
    public List<Anime> fetchAnimeList() {
        final List<Anime> sortList = Lists.newArrayList(animeList.values());
        Collections.sort(sortList, new MinimalEntryComByTitleAsc());
        return ImmutableList.copyOf(sortList);
    }


    @Override
    public boolean animeEntryExists(final String url) {
        return isInList(url);
    }


    /**
     * @since 2.7.0
     * @param anime
     * @return
     */
    private boolean isInList(final Anime anime) {
        return animeList.containsKey(anime.getId()) || isInList(anime.getInfoLink());
    }


    /**
     * @since 2.7.0
     * @param url
     * @return
     */
    private boolean isInList(final String url) {
        if (StringUtils.isNotBlank(url)) {
            for (final MinimalEntry curEntry : animeList.values()) {
                if (url.equalsIgnoreCase(curEntry.getInfoLink())) {
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


    /**
     * @since 2.7.0
     */
    public void clear() {
        animeList.clear();
    }


    @Override
    public void updateOrCreate(final Anime anime) {
        if (anime != null && anime.getId() != null) {
            animeList.put(anime.getId(), anime);
        }
    }
}
