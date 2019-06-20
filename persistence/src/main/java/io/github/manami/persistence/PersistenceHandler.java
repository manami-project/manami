package io.github.manami.persistence;

import java.util.List;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;

public interface PersistenceHandler extends ApplicationPersistence {

    void clearAll();


    void addAnimeList(List<Anime> list);


    void addFilterList(List<? extends MinimalEntry> list);


    void addWatchList(List<? extends MinimalEntry> list);
}