package io.github.manami.dto.events;

import javafx.stage.Stage;
import lombok.Getter;

/**
 * @author manami-project
 * @since 2.7.2
 */
public class ApplicationContextStartedEvent {

    @Getter
    private final Stage stage;


    public ApplicationContextStartedEvent(final Stage stage) {
        this.stage = stage;
    }
}
