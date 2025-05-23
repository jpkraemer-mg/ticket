package quest.darkoro.ticket.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Reward;
import quest.darkoro.ticket.persistence.model.RewardTier;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {

  List<Reward> findByTier(RewardTier tier);

  Optional<Reward> findByNameAndGuildId(String name, Long guildId);

  List<Reward> findByTierIsNullAndCreatedAtBefore(LocalDateTime createdAt);
}
