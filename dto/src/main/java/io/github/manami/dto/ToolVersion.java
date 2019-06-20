package io.github.manami.dto;

import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains the current tool version.
 */
@Slf4j
public final class ToolVersion {

    public static String getToolVersion() {
        final String propertiesPath = "/META-INF/maven/io.github.manami/persistence/pom.properties";

        try {
            final InputStream resourceStream = ToolVersion.class.getResourceAsStream(propertiesPath);
            final Properties properties = new Properties();
            properties.load(resourceStream);

            return properties.getProperty("version");
        } catch (final Exception e) {
            log.error("Could not determine software version: ", e);
        }

        return "unknown";
    }
}
