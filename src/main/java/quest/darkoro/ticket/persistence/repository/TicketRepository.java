package quest.darkoro.ticket.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import quest.darkoro.ticket.persistence.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
  List<Ticket> findAllByGuildId(Long guildId);

  Ticket getTicketByChannel(Long channel);
}
