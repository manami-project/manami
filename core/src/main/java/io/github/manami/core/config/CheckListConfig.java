package io.github.manami.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author manami-project
 * @since 2.6.0
 */
@AllArgsConstructor
public class CheckListConfig {

    @Getter
    private final boolean checkLocations;

    @Getter
    private final boolean checkCrc;

    @Getter
    private final boolean checkMetaData;
}
