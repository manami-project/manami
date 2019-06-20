package io.github.manami.dto.events;

import javafx.stage.Stage;
import lombok.Getter;

public class ApplicationContextStartedEvent {

    @Getter
    private final Stage stage;


    public ApplicationContextStartedEvent(final Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null in ApplicationContextStartedEvent");
        }

        this.stage = stage;
    }
}
