package quest.darkoro.ticket.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Selfrole;

@Repository
public interface SelfroleRepository extends JpaRepository<Selfrole, Integer> {
  List<Selfrole> findByGuildId(Long guildId);

  @Transactional
  void deleteByGuildIdAndRoleId(Long guildId, Long roleId);
}
