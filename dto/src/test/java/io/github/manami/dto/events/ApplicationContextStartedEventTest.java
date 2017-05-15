package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javafx.stage.Stage;
import org.testng.annotations.Test;

public class ApplicationContextStartedEventTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testApplicationContextStartedEventTest() {
    // given
    final Stage stage = mock(Stage.class);

    final ApplicationContextStartedEvent sut = new ApplicationContextStartedEvent(stage);

    // when
    final Stage result = sut.getStage();

    // then
    assertThat(sut).isNotNull();
    assertThat(result).isEqualTo(stage);
  }


  @Test(groups = UNIT_TEST_GROUP, expectedExceptions = IllegalArgumentException.class)
  public void testApplicationContextStartedEventTestWithNull() {
    // given

    // when
    new ApplicationContextStartedEvent(null);

    // then
  }
}
