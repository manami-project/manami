package io.github.manami.core.config;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.Test;

public class ConfigFileWatchdogTest {

    @Test(groups = "unitTest", expectedExceptions = { IllegalStateException.class })
    public void ConfigFileWatchdog() {
        // given

        // when
        new ConfigFileWatchdog(null);

        // then
    }


    @Test(groups = "unitTest")
    public void validate() throws IOException {
        // given
        final Path createTempDirectory = Files.createTempDirectory("test");

        // when
        new ConfigFileWatchdog(createTempDirectory).validate();

        // then
        final Path configFolder = createTempDirectory.resolve("config");
        final Path themeFolder = configFolder.resolve("theme");
        final Path dtdFile = configFolder.resolve("animelist.dtd");
        final Path transformationFile = themeFolder.resolve("animelist_transform.xsl");
        final Path stylesheetFile = themeFolder.resolve("style.css");

        assertTrue(Files.exists(configFolder));
        assertTrue(Files.isDirectory(configFolder));
        assertTrue(Files.exists(themeFolder));
        assertTrue(Files.isDirectory(themeFolder));
        assertTrue(Files.exists(dtdFile));
        assertTrue(Files.isRegularFile(dtdFile));
        assertTrue(Files.exists(transformationFile));
        assertTrue(Files.isRegularFile(transformationFile));
        assertTrue(Files.exists(stylesheetFile));
        assertTrue(Files.isRegularFile(stylesheetFile));
    }
}
