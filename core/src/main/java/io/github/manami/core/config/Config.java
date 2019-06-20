package io.github.manami.core.config;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.eventbus.EventBus;

import io.github.manami.dto.events.OpenedFileChangedEvent;
import javafx.util.Duration;
import lombok.Getter;

/**
 * Contains the path for all configuration files as well as the path for the
 * currently opened anime list file.
 */
@Named
public class Config {

    /** File which is currently being worked on. */
    @Getter
    private Path file;

    public static final Duration NOTIFICATION_DURATION = Duration.seconds(6.0);

    private final EventBus eventBus;


    @Inject
    public Config(final EventBus eventBus) {
        this.eventBus = eventBus;
    }


    /**
     * @param file the file to set
     */
    public void setFile(final Path file) {
        this.file = file;
        eventBus.post(new OpenedFileChangedEvent());
    }
}
