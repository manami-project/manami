package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class AbstractEvent implements Event {

    @Getter
    @Setter
    private EventType type;

    @Getter
    @Setter
    private Anime anime;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String message;

    public enum EventType {
        ERROR, WARNING, INFO
    }


    public AbstractEvent(final Anime anime) {
        type = EventType.INFO;
        this.anime = anime;
        title = anime.getTitle();
    }


    public AbstractEvent() {
        type = EventType.INFO;
    }
}
