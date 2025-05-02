package quest.darkoro.ticket.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.RewardTier;

@Repository
public interface RewardTierRepository extends JpaRepository<RewardTier, UUID> {

  List<RewardTier> findByGuildId(Long guildId);
}
