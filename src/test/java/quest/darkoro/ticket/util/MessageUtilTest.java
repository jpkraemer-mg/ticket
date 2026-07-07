package quest.darkoro.ticket.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import quest.darkoro.ticket.persistence.CategoryRepository;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.persistence.SelfroleRepository;
import quest.darkoro.ticket.persistence.model.Guild;

class MessageUtilTest {

  private static final Long GID = 1L;
  private static final Long LOG_CHANNEL = 5L;

  private GuildRepository guildRepository;
  private MessageUtil messageUtil;
  private net.dv8tion.jda.api.entities.Guild jdaGuild;
  private TextChannel logChannel;

  @BeforeEach
  void setUp() {
    guildRepository = mock(GuildRepository.class);
    messageUtil = new MessageUtil(mock(CategoryRepository.class), mock(SelfroleRepository.class),
        guildRepository);
    jdaGuild = mock(net.dv8tion.jda.api.entities.Guild.class);
    when(jdaGuild.getIdLong()).thenReturn(GID);
    logChannel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void sendLogMessageWithDeletedChannelDoesNotThrow() {
    assertThatCode(() -> messageUtil.sendLogMessage("hello", null))
        .doesNotThrowAnyException();
  }

  @Test
  void sendGuildLogDoesNothingWithoutGuildRow() {
    when(guildRepository.findById(GID)).thenReturn(Optional.empty());

    messageUtil.sendGuildLog(jdaGuild, "hello");

    verify(jdaGuild, never()).getTextChannelById(org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void sendGuildLogDoesNothingWhenNoLogChannelConfigured() {
    when(guildRepository.findById(GID)).thenReturn(Optional.of(new Guild().setId(GID)));

    assertThatCode(() -> messageUtil.sendGuildLog(jdaGuild, "hello"))
        .doesNotThrowAnyException();
    verify(jdaGuild, never()).getTextChannelById(org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void sendGuildLogSendsToConfiguredChannel() {
    when(guildRepository.findById(GID))
        .thenReturn(Optional.of(new Guild().setId(GID).setLog(LOG_CHANNEL)));
    when(jdaGuild.getTextChannelById(LOG_CHANNEL)).thenReturn(logChannel);

    messageUtil.sendGuildLog(jdaGuild, "hello");

    verify(logChannel).sendMessage("hello");
  }

  @Test
  void sendGuildLogSurvivesDeletedLogChannel() {
    when(guildRepository.findById(GID))
        .thenReturn(Optional.of(new Guild().setId(GID).setLog(LOG_CHANNEL)));
    when(jdaGuild.getTextChannelById(LOG_CHANNEL)).thenReturn(null);

    assertThatCode(() -> messageUtil.sendGuildLog(jdaGuild, "hello"))
        .doesNotThrowAnyException();
  }

  @Test
  void sendCommandLogUsesTheSharedFormat() {
    when(guildRepository.findById(GID))
        .thenReturn(Optional.of(new Guild().setId(GID).setLog(LOG_CHANNEL)));
    when(jdaGuild.getTextChannelById(LOG_CHANNEL)).thenReturn(logChannel);
    var member = mock(Member.class);
    when(member.getGuild()).thenReturn(jdaGuild);
    when(member.getEffectiveName()).thenReturn("Alice");
    when(member.getIdLong()).thenReturn(42L);

    messageUtil.sendCommandLog(member, "/filter add", "ADD MESSAGE CONTENT FILTER: `x`");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(logChannel).sendMessage(captor.capture());
    assertThat(captor.getValue()).isEqualTo(
        "Command `/filter add` executed by `Alice (42)`\nADD MESSAGE CONTENT FILTER: `x`");
  }

  @Test
  void refreshTicketMessageDoesNothingWithoutConfiguredBaseChannel() {
    when(guildRepository.findById(GID)).thenReturn(Optional.of(new Guild().setId(GID)));

    assertThatCode(() -> messageUtil.refreshTicketMessage(jdaGuild, null))
        .doesNotThrowAnyException();
    verify(jdaGuild, never()).getTextChannelById(org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void refreshRoleMessageDoesNothingWithoutConfiguredRoleChannel() {
    when(guildRepository.findById(GID)).thenReturn(Optional.of(new Guild().setId(GID)));

    assertThatCode(() -> messageUtil.refreshRoleMessage(jdaGuild, null))
        .doesNotThrowAnyException();
    verify(jdaGuild, never()).getTextChannelById(org.mockito.ArgumentMatchers.anyLong());
  }
}
