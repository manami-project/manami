package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;

public class SimpleLocationEvent extends AbstractEvent {

    public SimpleLocationEvent(final Anime anime) {
        super(anime);
    }
}
