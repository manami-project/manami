package io.github.manami.core.services.events;

import io.github.manami.dto.entities.Anime;

/**
 * @author manami project
 * @since 2.6.0
 */
public class AbstractEvent implements Event {

    private EventType type;

    private String title;

    private String message;

    public enum EventType {
        ERROR, WARNING, INFO
    }


    public AbstractEvent(final Anime anime) {
        type = EventType.INFO;
        title = anime.getTitle();
    }


    public AbstractEvent() {
        type = EventType.INFO;
    }


    /**
     * @return the type
     */
    @Override
    public EventType getType() {
        return type;
    }


    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }


    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }


    /**
     * @param type
     *            the type to set
     */
    @Override
    public void setType(final EventType type) {
        this.type = type;
    }


    @Override
    public void setTitle(final String title) {
        this.title = title;
    }


    @Override
    public void setMessage(final String message) {
        this.message = message;
    }
}
