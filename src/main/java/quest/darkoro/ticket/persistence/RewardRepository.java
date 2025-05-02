package quest.darkoro.ticket.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Reward;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {
}
