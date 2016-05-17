package io.github.manami.persistence;

import java.util.List;
import java.util.UUID;

import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.7.0
 */
public interface AnimeListHandler {

    /**
     * Adds an {@link Anime} if it is not already in the list.
     * @since 2.7.0
     * @param anime
     * Anime to add to the list of watched animes.
     * @return true if the anime was added.
     */
    boolean addAnime(Anime anime);


    /**
     * @since 2.7.0
     * @return A {@link List} of {@link Anime}s which have been watched.
     */
    List<Anime> fetchAnimeList();


    /**
     * @since 2.7.0
     * @param url
     * URL of the anime's infolink.
     * @return true if an entry with this URL as infolink already exists.
     */
    boolean animeEntryExists(String url);


    /**
     * @since 2.7.0
     * @param id
     * ID of the anime.
     * @return true if an entry was removed.
     */
    boolean removeAnime(UUID id);
}
