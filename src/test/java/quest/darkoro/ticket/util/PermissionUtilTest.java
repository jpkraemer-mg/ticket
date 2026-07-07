package quest.darkoro.ticket.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import quest.darkoro.ticket.persistence.AdministratorRepository;
import quest.darkoro.ticket.persistence.CategoryRepository;
import quest.darkoro.ticket.persistence.model.Administrator;
import quest.darkoro.ticket.persistence.model.Category;

class PermissionUtilTest {

  private static final Long GID = 1L;

  private AdministratorRepository administratorRepository;
  private CategoryRepository categoryRepository;
  private PermissionUtil permissionUtil;
  private IReplyCallback event;
  private Guild guild;
  private Member member;

  @BeforeEach
  void setUp() {
    administratorRepository = mock(AdministratorRepository.class);
    categoryRepository = mock(CategoryRepository.class);
    permissionUtil = new PermissionUtil(administratorRepository, mock(JDA.class),
        categoryRepository);
    event = mock(IReplyCallback.class, RETURNS_DEEP_STUBS);
    guild = mock(Guild.class);
    member = mock(Member.class);
    when(event.getGuild()).thenReturn(guild);
  }

  @Test
  void administratorIsPermittedWithoutTouchingRepository() {
    when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);

    assertThat(permissionUtil.isPermitted(event, GID, member)).isTrue();
    verifyNoInteractions(administratorRepository);
    verify(event, never()).reply(anyString());
  }

  @Test
  void nonAdminIsDeniedWhenNoAdminRolesConfigured() {
    when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
    when(administratorRepository.getAllByGuildId(GID)).thenReturn(List.of());

    assertThat(permissionUtil.isPermitted(event, GID, member)).isFalse();

    var captor = ArgumentCaptor.forClass(String.class);
    verify(event).reply(captor.capture());
    assertThat(captor.getValue()).contains("administrator");
  }

  @Test
  void memberWithConfiguredAdminRoleIsPermitted() {
    var role = mock(Role.class);
    when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
    when(member.getRoles()).thenReturn(List.of(role));
    when(administratorRepository.getAllByGuildId(GID))
        .thenReturn(List.of(new Administrator().setGuildId(GID).setRoleId(10L)));
    when(guild.getRoleById(10L)).thenReturn(role);

    assertThat(permissionUtil.isPermitted(event, GID, member)).isTrue();
    verify(event, never()).reply(anyString());
  }

  @Test
  void deletedAdminRoleDoesNotCrashAndDenialListsSurvivingRoles() {
    var survivingRole = mock(Role.class);
    when(survivingRole.getAsMention()).thenReturn("@surviving");
    when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
    when(member.getRoles()).thenReturn(List.of());
    when(administratorRepository.getAllByGuildId(GID)).thenReturn(List.of(
        new Administrator().setGuildId(GID).setRoleId(10L),
        new Administrator().setGuildId(GID).setRoleId(20L)
    ));
    when(guild.getRoleById(10L)).thenReturn(null);
    when(guild.getRoleById(20L)).thenReturn(survivingRole);

    assertThat(permissionUtil.isPermitted(event, GID, member)).isFalse();

    var captor = ArgumentCaptor.forClass(String.class);
    verify(event).reply(captor.capture());
    assertThat(captor.getValue()).contains("@surviving");
  }

  @Test
  void validCategoryMatchesOnlyConfiguredCategoryIds() {
    when(categoryRepository.findByGuildId(GID))
        .thenReturn(List.of(new Category().setId(5L).setGuildId(GID).setName("BUG")));

    assertThat(permissionUtil.validCategory(GID, 5L)).isTrue();
    assertThat(permissionUtil.validCategory(GID, 6L)).isFalse();
  }
}
