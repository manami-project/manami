package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;

/**
 * @author manami-project
 * @since 2.6.0
 */
public class AbstractEvent implements Event {

    private EventType type;

    private Anime anime;

    private String title;

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


    @Override
    public EventType getType() {
        return type;
    }


    @Override
    public String getMessage() {
        return message;
    }


    @Override
    public void setType(final EventType type) {
        this.type = type;
    }


    @Override
    public void setMessage(final String message) {
        this.message = message;
    }


    @Override
    public Anime getAnime() {
        return anime;
    }


    @Override
    public void setAnime(final Anime anime) {
        this.anime = anime;
    }


    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public void setTitle(final String title) {
        this.title = title;
    }
}
