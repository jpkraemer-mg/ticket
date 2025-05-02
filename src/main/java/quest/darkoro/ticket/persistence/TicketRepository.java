package quest.darkoro.ticket.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
  List<Ticket> findAllByGuildId(Long guildId);

  Ticket getTicketByChannel(Long channel);
}
