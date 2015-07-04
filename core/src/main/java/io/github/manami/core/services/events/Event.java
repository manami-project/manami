package io.github.manami.core.services.events;

import io.github.manami.core.services.events.AbstractEvent.EventType;

/**
 * @author manami project
 * @since 2.6.0
 */
public interface Event {

    EventType getType();


    String getTitle();


    String getMessage();


    void setType(final EventType type);


    void setTitle(final String title);


    void setMessage(final String message);
}
