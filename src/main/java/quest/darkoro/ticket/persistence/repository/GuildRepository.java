package quest.darkoro.ticket.persistence.repository;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import quest.darkoro.ticket.persistence.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRepository extends JpaRepository<Guild, Long> {
  @NotNull Optional<Guild> findById(Long id);
}
