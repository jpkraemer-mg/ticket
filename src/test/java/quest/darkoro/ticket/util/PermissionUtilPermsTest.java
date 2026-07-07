package quest.darkoro.ticket.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PermissionUtilPermsTest {

  private final PermissionUtil permissionUtil = new PermissionUtil(null, null, null);

  @Test
  void filteredDenyContainsNoAllowedPermission() {
    assertThat(permissionUtil.getFilteredDeny())
        .doesNotContainAnyElementsOf(permissionUtil.getAllow());
  }

  @Test
  void filteredDenyIsDenyMinusAllow() {
    var expected = permissionUtil.getDeny();
    expected.removeAll(permissionUtil.getAllow());
    assertThat(permissionUtil.getFilteredDeny()).isEqualTo(expected);
  }

  @Test
  void botPermissionsEqualDeny() {
    assertThat(permissionUtil.getBotPermissions()).isEqualTo(permissionUtil.getDeny());
  }
}
