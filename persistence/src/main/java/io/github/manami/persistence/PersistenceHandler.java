package io.github.manami.persistence;

import java.util.List;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;

/**
 * @author manami-project
 * @since 2.0.0
 */
public interface PersistenceHandler extends ApplicationPersistence {

    /**
     * @since 2.7.0
     */
    void clearAll();


    /**
     * @since 2.7.2
     * @param list
     */
    void addAnimeList(List<Anime> list);


    /**
     * @since 2.7.2
     * @param list
     */
    void addFilterList(List<? extends MinimalEntry> list);


    /**
     * @since 2.8.0
     * @param list
     */
    void addWatchList(List<? extends MinimalEntry> list);
}
