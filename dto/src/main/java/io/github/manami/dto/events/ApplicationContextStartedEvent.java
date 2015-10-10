package io.github.manami.dto.events;

import javafx.stage.Stage;

/**
 * @author manami-project
 * @since 2.7.2
 */
public class ApplicationContextStartedEvent {

    private final Stage stage;


    public ApplicationContextStartedEvent(final Stage stage) {
        this.stage = stage;
    }


    /**
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }
}
