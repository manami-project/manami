package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class AnimeListChangedEventTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testAnimeListChangedEventIsInstantiable() {
    // given

    // when

    // then
    assertThat(new AnimeListChangedEvent()).isNotNull();
  }
}
