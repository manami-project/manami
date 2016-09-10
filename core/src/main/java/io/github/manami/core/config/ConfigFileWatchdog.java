package io.github.manami.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigFileWatchdog {

    private static final String STYLESHEET_PLACEHOLDER = "{{STYLESHEET-FILE-PATH}}";
    private final Path configFolder;
    private final Path themePath;
    private final Path stylesheetFile;


    public ConfigFileWatchdog(final Path path) {
        if (path == null) {
            throw new IllegalStateException("Current path cannot be null!");
        }

        configFolder = path.resolve(Paths.get("config"));
        themePath = configFolder.resolve(Paths.get("theme"));
        stylesheetFile = themePath.resolve(Paths.get("style.css"));
    }


    public void validate() throws IOException {
        checkConfigFolder();
        checkThemeFolder();
        checkDtdFile();
        checkStylesheetFile();
        checkTransformationFile();
    }


    private void checkThemeFolder() throws IOException {
        createDirectoryIfNotExist(themePath);
    }


    private void checkConfigFolder() throws IOException {
        createDirectoryIfNotExist(configFolder);
    }


    private void createDirectoryIfNotExist(final Path dir) throws IOException {
        final Path folderName = dir.getFileName();

        log.info("Checking folder [{}]", folderName);

        if (!Files.exists(dir)) {
            log.info("Folder [{}] does not exist, creating it.", folderName);
            Files.createDirectory(dir);
            log.info("Folder [{}] created under [{}]", folderName, dir.toAbsolutePath());
        }

        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            throw new IllegalStateException("Config folder does not exist, but a file with the same name.");
        }
    }


    private void checkDtdFile() throws IOException {
        final Path animeDtd = configFolder.resolve(Paths.get("animelist.dtd"));
        createFileIfNotExist(animeDtd);
    }


    private void checkTransformationFile() throws IOException {
        final Path transformationFile = themePath.resolve(Paths.get("animelist_transform.xsl"));
        createFileIfNotExist(transformationFile);

        final List<String> transformationFileAsLines = Files.readAllLines(transformationFile);
        final String absoluteStylesheetPath = themePath.resolve(Paths.get("style.css")).toAbsolutePath().toUri().toURL().toString();

        for (int i = 0; i < transformationFileAsLines.size(); i++) {
            final String line = transformationFileAsLines.get(i);
            if (line.contains(STYLESHEET_PLACEHOLDER)) {
                transformationFileAsLines.set(i, line.replace(STYLESHEET_PLACEHOLDER, absoluteStylesheetPath));
            }
        }

        // remove strange preamble which only is set in this file and only in
        // when starting a fat jar
        String firstLine = transformationFileAsLines.get(0);

        while (firstLine.charAt(0) != '<' && firstLine.length() > 0) {
            firstLine = firstLine.substring(1);
        }

        transformationFileAsLines.set(0, firstLine);

        Files.write(transformationFile, transformationFileAsLines, StandardCharsets.UTF_8);
    }


    private void checkStylesheetFile() throws IOException {
        createFileIfNotExist(stylesheetFile);
    }


    private void createFileIfNotExist(final Path file) throws IOException {
        final Path fileName = file.getFileName();

        log.info("Checking file [{}]", fileName);

        if (!Files.exists(file)) {
            log.info("File [{}] does not exist, creating it.", fileName);
            Files.createFile(file);

            final String resourceFilename = String.format("releasebuild_%s", file.getFileName().toString());
            final String resourceFileAsString = IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceFilename));
            final List<String> fileAsLines = Arrays.asList(resourceFileAsString.split("\\r?\\n"));
            Files.write(file, fileAsLines, StandardCharsets.UTF_8);

            log.info("File [{}] created under [{}]", fileName, file.toAbsolutePath());
        }
    }
}
