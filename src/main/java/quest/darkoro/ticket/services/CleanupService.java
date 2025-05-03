package quest.darkoro.ticket.services;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.RewardRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {
  private final RewardRepository rewardRepository;

  @Scheduled(cron = "0 0 * * * *")
  public void cleanOrphanedRewards() {
    var cutoff = LocalDateTime.now().minusMinutes(60);
    var orphaned = rewardRepository.findByTierIsNullAndCreatedAtBefore(cutoff);
    if (!orphaned.isEmpty()) {
      log.info("Deleting {} orphaned rewards", orphaned.size());
      rewardRepository.deleteAll(orphaned);
    }
  }
}
