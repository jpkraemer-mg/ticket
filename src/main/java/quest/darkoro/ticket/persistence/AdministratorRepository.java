package quest.darkoro.ticket.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import quest.darkoro.ticket.persistence.model.Administrator;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, UUID> {
  @Transactional
  void removeByRoleId(Long roleId);

  List<Administrator> getAllByGuildId(Long guildId);
}
