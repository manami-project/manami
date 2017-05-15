package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class OpenedFileChangedEventTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testOpenedFileChangedEventTestIsInstantiable() {
    // given

    // when

    // then
    assertThat(new OpenedFileChangedEvent()).isNotNull();
  }
}
