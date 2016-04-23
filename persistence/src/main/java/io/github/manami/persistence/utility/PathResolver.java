package io.github.manami.persistence.utility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to create, check and resolve an absolute path to a relative path if
 * necessary.
 *
 * @author manami-project
 * @since 2.10.0
 */
@Slf4j
public class PathResolver {

    /**
     * @since 2.10.0
     * @param path
     * @return
     */
    public static Optional<Path> buildPath(final String path, final Path currentWorkingDir) {
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) { // absolute
            dir = createRelativePath(dir, currentWorkingDir);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) { // relative
                return Optional.empty();
            }
        }

        return Optional.of(dir);
    }


    /**
     * @since 2.10.0
     * @param path
     * @param currentWorkingDir
     * @return
     */
    public static String buildRelativizedPath(final String path, final Path currentWorkingDir) {
        final Optional<Path> optDir = buildPath(path, currentWorkingDir);

        if (optDir.isPresent()) {
            try {
                return currentWorkingDir.relativize(optDir.get()).toString().replace("\\", "/");
            } catch (final IllegalArgumentException e) {
                return path;
            }
        }

        return null;
    }


    /**
     * Creates a relative path.
     *
     * @since 2.6.0
     * @param dir
     * @return
     */
    private static Path createRelativePath(final Path dir, final Path currentWorkingDir) {
        Path ret = null;
        try {
            ret = currentWorkingDir.resolve(dir);
        } catch (final Exception e) {
            log.error("An error occurred trying to create a relative Path: ", e);
        }

        return ret;
    }
}
