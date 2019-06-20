package io.github.manami.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CheckListConfig {

    @Getter
    private final boolean checkLocations;

    @Getter
    private final boolean checkCrc;

    @Getter
    private final boolean checkMetaData;

    @Getter
    private final boolean checkDeadEntries;
}
