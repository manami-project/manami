package io.github.manami.core.config;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.Test;

public class ConfigFileWatchdogTest {

  @Test(groups = UNIT_TEST_GROUP, expectedExceptions = {IllegalStateException.class})
  public void ConfigFileWatchdog() {
    // given

    // when
    new ConfigFileWatchdog(null);

    // then
  }


  @Test(groups = UNIT_TEST_GROUP)
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

    assertThat(Files.exists(configFolder)).isTrue();
    assertThat(Files.isDirectory(configFolder)).isTrue();
    assertThat(Files.exists(themeFolder)).isTrue();
    assertThat(Files.isDirectory(themeFolder)).isTrue();
    assertThat(Files.exists(dtdFile)).isTrue();
    assertThat(Files.isRegularFile(dtdFile)).isTrue();
    assertThat(Files.exists(transformationFile)).isTrue();
    assertThat(Files.isRegularFile(transformationFile)).isTrue();
    assertThat(Files.exists(stylesheetFile)).isTrue();
    assertThat(Files.isRegularFile(stylesheetFile)).isTrue();
  }
}
