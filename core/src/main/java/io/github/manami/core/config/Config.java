package io.github.manami.core.config;

import com.google.common.eventbus.EventBus;
import javafx.util.Duration;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains the path for all configuration files as well as the path for the
 * currently opened anime list file.
 *
 * @author manami project
 * @since 2.0.0
 */
@Named
public class Config {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    /** File which is currently being worked on. */
    private Path file;

    /** Path for the config files. */
    private String configFilesPath;

    public static final Duration NOTIFICATION_DURATION = Duration.seconds(6.0);

    private final EventBus eventBus;


    /**
     * Constructor.
     *
     * @since 2.0.0
     */
    @Inject
    public Config(final EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @PostConstruct
    private void init() {
        String path = Config.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }

        path = Paths.get(path).getParent().toAbsolutePath().toString();
        path = path.replaceAll("\\\\", "/");

        try {
            configFilesPath = "file:///" + URLDecoder.decode(path, "UTF-8") + "/config";
        } catch (final UnsupportedEncodingException e) {
            LOG.error("An error occurred trying to determine the path for the configuration files: ", e);
        }
    }


    /**
     * @since 2.0.0
     * @return the file
     */
    public Path getFile() {
        return file;
    }


    /**
     * @since 2.0.0
     * @param file
     *            the file to set
     */
    public void setFile(final Path file) {
        this.file = file;
        eventBus.post(new OpenedFileChangedEvent());
    }


    /**
     * @since 2.0.0
     * @return the configFilesPath
     */
    public String getConfigFilesPath() {
        return configFilesPath;
    }


    /**
     * @since 2.0.0
     * @param configFilesPath
     *            the configFilesPath to set
     */
    public void setConfigFilesPath(final String configFilesPath) {
        this.configFilesPath = configFilesPath;
    }
}
