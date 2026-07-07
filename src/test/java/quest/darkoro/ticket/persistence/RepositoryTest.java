package quest.darkoro.ticket.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import quest.darkoro.ticket.persistence.model.Category;
import quest.darkoro.ticket.persistence.model.Reward;
import quest.darkoro.ticket.persistence.model.RewardTier;
import quest.darkoro.ticket.persistence.model.Selfrole;
import quest.darkoro.ticket.persistence.model.Ticket;

@DataJpaTest
class RepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private RewardRepository rewardRepository;
  @Autowired
  private RewardTierRepository rewardTierRepository;
  @Autowired
  private TicketRepository ticketRepository;
  @Autowired
  private SelfroleRepository selfroleRepository;

  @Test
  void categoryFindByNameAndGuildIdReturnsOptional() {
    categoryRepository.save(new Category()
        .setId(10L).setName("BUG").setGuildId(1L).setDescription("d").setMentions("m"));

    assertThat(categoryRepository.findByNameAndGuildId("BUG", 1L)).isPresent();
    assertThat(categoryRepository.findByNameAndGuildId("BUG", 2L)).isEmpty();
    assertThat(categoryRepository.findByNameAndGuildId("OTHER", 1L)).isEmpty();
  }

  @Test
  void orphanedRewardQueryOnlyFindsOldTierlessRewards() {
    var tier = rewardTierRepository.save(new RewardTier().setName("T1").setGuildId(1L));
    rewardRepository.save(new Reward().setName("attached").setGuildId(1L).setTier(tier)
        .setCreatedAt(LocalDateTime.now().minusHours(2)));
    rewardRepository.save(new Reward().setName("fresh orphan").setGuildId(1L)
        .setCreatedAt(LocalDateTime.now()));
    rewardRepository.save(new Reward().setName("old orphan").setGuildId(1L)
        .setCreatedAt(LocalDateTime.now().minusHours(2)));

    var orphaned = rewardRepository
        .findByTierIsNullAndCreatedAtBefore(LocalDateTime.now().minusMinutes(60));

    assertThat(orphaned).extracting(Reward::getName).containsExactly("old orphan");
  }

  @Test
  void ticketLookupByChannel() {
    ticketRepository.save(new Ticket()
        .setCreator(1L).setTitle("t").setDescription("d").setChannel(42L).setGuildId(1L));

    assertThat(ticketRepository.getTicketByChannel(42L)).isNotNull();
    assertThat(ticketRepository.getTicketByChannel(43L)).isNull();
  }

  @Test
  void selfroleDeleteOnlyRemovesTheMatchingRow() {
    selfroleRepository.save(new Selfrole().setGuildId(1L).setRoleId(100L));
    selfroleRepository.save(new Selfrole().setGuildId(1L).setRoleId(200L));
    selfroleRepository.save(new Selfrole().setGuildId(2L).setRoleId(100L));

    selfroleRepository.deleteByGuildIdAndRoleId(1L, 100L);

    assertThat(selfroleRepository.findByGuildId(1L))
        .extracting(Selfrole::getRoleId).containsExactly(200L);
    assertThat(selfroleRepository.findByGuildId(2L)).hasSize(1);
  }

  @Test
  void rewardFindByNameAndGuildIdIsGuildScoped() {
    rewardRepository.save(new Reward().setName("Cookie").setGuildId(1L));

    assertThat(rewardRepository.findByNameAndGuildId("Cookie", 1L)).isPresent();
    assertThat(rewardRepository.findByNameAndGuildId("Cookie", 2L)).isEmpty();
  }
}
