package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class LocationEvent extends AbstractEvent {

    public LocationEvent(final Anime anime) {
        super(anime);
    }
}
