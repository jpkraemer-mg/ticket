package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
public class Ticket {

  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private Long creator;

  @Column
  private String title;

  @Column(length = 820)
  private String description;

  @Column
  private Long channel;

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_TICKET_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;
}
