package io.github.manami.core.services.events;

import io.github.manami.core.services.events.AbstractEvent.EventType;
import io.github.manami.dto.entities.Anime;

public interface Event {

    EventType getType();


    Anime getAnime();


    String getTitle();


    String getMessage();


    void setType(final EventType type);


    void setTitle(final String title);


    void setAnime(final Anime anime);


    void setMessage(final String message);
}
