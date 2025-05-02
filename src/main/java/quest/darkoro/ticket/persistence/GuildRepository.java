package quest.darkoro.ticket.persistence;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
  @NotNull Optional<Guild> findById(Long id);
}
