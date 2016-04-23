package io.github.manami.dto;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the current tool version.
 *
 * @author manami-project
 * @since 2.1.0
 */
public class ToolVersion {

    private static final Logger LOG = LoggerFactory.getLogger(ToolVersion.class);


    /**
     * @since 2.7.0
     */
    public static String getToolVersion() {
        final String propertiesPath = "/META-INF/maven/io.github.manami/persistence/pom.properties";

        try {
            final InputStream resourceStream = ToolVersion.class.getResourceAsStream(propertiesPath);
            final Properties properties = new Properties();
            properties.load(resourceStream);

            return properties.getProperty("version");
        } catch (final Exception e) {
            LOG.error("Could not determine software version: ", e);
        }

        return "unknown";
    }
}
