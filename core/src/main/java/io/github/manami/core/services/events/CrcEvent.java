package io.github.manami.core.services.events;

import java.nio.file.Path;

import lombok.Getter;
import lombok.Setter;

/**
 * @author manami-project
 *
 */
public class CrcEvent extends AbstractEvent {

    @Getter
    @Setter
    private Path path;

    @Getter
    @Setter
    private String crcSum;
}
