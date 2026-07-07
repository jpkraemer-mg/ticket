package quest.darkoro.ticket.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DataUtilTest {

  private final DataUtil dataUtil = new DataUtil();

  @Test
  void fixUuidTurnsRawHexIntoDashedUuid() {
    assertThat(dataUtil.fixUUID("069a79f444e94726a5befca90e38aaf5"))
        .isEqualTo("069a79f4-44e9-4726-a5be-fca90e38aaf5");
  }

  @Test
  void fixUuidKeepsAlreadyDashedUuidIntact() {
    assertThat(dataUtil.fixUUID("069a79f4-44e9-4726-a5be-fca90e38aaf5"))
        .isEqualTo("069a79f4-44e9-4726-a5be-fca90e38aaf5");
  }

  @Test
  void fixUuidRejectsGarbage() {
    assertThatThrownBy(() -> dataUtil.fixUUID("not-a-uuid"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
