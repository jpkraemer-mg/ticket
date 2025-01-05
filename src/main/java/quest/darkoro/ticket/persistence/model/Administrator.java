package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Entity
@Accessors(chain = true)
public class Administrator {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column
  private Long roleId;

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_ADMINISTRATOR_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;

  @Column
  private String name;
}
