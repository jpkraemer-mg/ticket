package quest.darkoro.ticket.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import quest.darkoro.ticket.persistence.RewardRepository;
import quest.darkoro.ticket.persistence.RewardTierRepository;
import quest.darkoro.ticket.persistence.SelfroleRepository;
import quest.darkoro.ticket.persistence.TicketRepository;
import quest.darkoro.ticket.persistence.model.Reward;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

class SelectMenuServiceTest {

  private MessageUtil messageUtil;
  private RewardRepository rewardRepository;
  private RewardTierRepository rewardTierRepository;
  private SelectMenuService service;

  @BeforeEach
  void setUp() {
    messageUtil = mock(MessageUtil.class);
    rewardRepository = mock(RewardRepository.class);
    rewardTierRepository = mock(RewardTierRepository.class);
    service = new SelectMenuService(mock(SelfroleRepository.class), messageUtil,
        mock(PermissionUtil.class), mock(TicketRepository.class), rewardRepository,
        rewardTierRepository);
  }

  private StringSelectInteractionEvent event(String componentId, String selectedValue) {
    var e = mock(StringSelectInteractionEvent.class, RETURNS_DEEP_STUBS);
    when(e.getComponentId()).thenReturn(componentId);
    when(e.getSelectedOptions().get(0).getValue()).thenReturn(selectedValue);
    return e;
  }

  @Test
  void chooseRewardRejectsAnyoneButTheTicketCreator() {
    var e = event("choosereward_123", "none");
    when(e.getUser().getId()).thenReturn("999");

    service.distributeEvent(e);

    verify(e).reply(contains("not permitted"));
    verify(rewardRepository, org.mockito.Mockito.never()).findById(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void chooseRewardCreatorMayDecline() {
    var e = event("choosereward_123", "none");
    when(e.getUser().getId()).thenReturn("123");

    service.distributeEvent(e);

    verify(e).reply(contains("will not get a reward"));
    verify(e.getChannel().asTextChannel()).sendMessage("The user chose not to get a reward.");
  }

  @Test
  void chooseRewardHandlesDeletedRewardGracefully() {
    var rewardId = UUID.randomUUID();
    var e = event("choosereward_123", rewardId.toString());
    when(e.getUser().getId()).thenReturn("123");
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());

    assertThatCode(() -> service.distributeEvent(e)).doesNotThrowAnyException();
    verify(e).reply(contains("might've been deleted"));
  }

  @Test
  void deleteRewardHandlesMissingRowWithoutThrowing() {
    var rewardId = UUID.randomUUID();
    var e = event("deletereward", rewardId.toString());
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());

    assertThatCode(() -> service.distributeEvent(e)).doesNotThrowAnyException();
    verify(e).reply(contains("might've been deleted"));
  }

  @Test
  void deleteRewardWithOrphanedTierLogsNoneInsteadOfCrashing() {
    var rewardId = UUID.randomUUID();
    var e = event("deletereward", rewardId.toString());
    var orphan = new Reward().setId(rewardId).setName("Cookie").setGuildId(1L);
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(orphan));

    assertThatCode(() -> service.distributeEvent(e)).doesNotThrowAnyException();

    verify(rewardRepository).delete(orphan);
    var details = ArgumentCaptor.forClass(String.class);
    verify(messageUtil).sendCommandLog(eq(e.getMember()), eq("/reward delete"), details.capture());
    assertThat(details.getValue()).contains("Cookie").contains("Tier `none`");
  }

  @Test
  void deleteRewardChooseTierHandlesDeletedTierGracefully() {
    var tierId = UUID.randomUUID();
    var e = event("deletechoosetier", tierId.toString());
    when(rewardTierRepository.findById(tierId)).thenReturn(Optional.empty());

    assertThatCode(() -> service.distributeEvent(e)).doesNotThrowAnyException();
    verify(e).reply(contains("might've been deleted"));
  }
}
