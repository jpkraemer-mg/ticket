package quest.darkoro.ticket.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quest.darkoro.ticket.persistence.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByNameAndGuildId(String name, Long guildId);

  List<Category> findByGuildId(Long guildId);
}
