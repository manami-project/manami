package io.github.manami.dto;

import java.io.InputStream;
import java.util.Properties;

/**
 * Contains the current tool version.
 *
 * @author manami project
 * @since 2.1.0
 */
public class ToolVersion {

    /**
     * @since 2.7.0
     */
    public static String getVersion() {
        final String propertiesPath = "/META-INF/maven/io.github.manami/persistence/pom.properties";

        try {
            final InputStream resourceStream = ToolVersion.class.getResourceAsStream(propertiesPath);
            final Properties properties = new Properties();
            properties.load(resourceStream);

            return properties.getProperty("version");
        } catch (final Exception e) {
            // TODO: can't load pom.properties => version information will be unavailable
        }

        return "unknown";
    }
}
