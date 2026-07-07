package quest.darkoro.ticket.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import quest.darkoro.ticket.persistence.RewardRepository;
import quest.darkoro.ticket.persistence.model.Reward;

class CleanupServiceTest {

  private final RewardRepository rewardRepository = mock(RewardRepository.class);
  private final CleanupService cleanupService = new CleanupService(rewardRepository);

  @Test
  void orphanedRewardsAreDeleted() {
    var orphans = List.of(new Reward().setName("stale"));
    when(rewardRepository.findByTierIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
        .thenReturn(orphans);

    cleanupService.cleanOrphanedRewards();

    verify(rewardRepository).deleteAll(orphans);
  }

  @Test
  void nothingIsDeletedWhenNoOrphansExist() {
    when(rewardRepository.findByTierIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
        .thenReturn(List.of());

    cleanupService.cleanOrphanedRewards();

    verify(rewardRepository, never()).deleteAll(anyList());
  }
}
