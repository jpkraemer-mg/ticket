package quest.darkoro.ticket.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.ContentFilter;

@Repository
public interface ContentFilterRepository extends JpaRepository<ContentFilter, UUID> {
  List<ContentFilter> findByGuildId(Long guildId);
}
