package io.github.manami.persistence;

import java.util.List;
import java.util.UUID;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

public interface AnimeListHandler {

    /**
     * Adds an {@link Anime} if it is not already in the list.
     * 
     * @param anime Anime to add to the list of watched anime.
     * @return true if the anime was added.
     */
    boolean addAnime(Anime anime);


    /**
     * @return A {@link List} of {@link Anime}s which have been watched.
     */
    List<Anime> fetchAnimeList();


    /**
     * @param infoLink URL of the anime's infolink.
     * @return true if an entry with this URL as infolink already exists.
     */
    boolean animeEntryExists(InfoLink infoLink);


    /**
     * @param id ID of the anime.
     * @return true if an entry was removed.
     */
    boolean removeAnime(UUID id);
}
