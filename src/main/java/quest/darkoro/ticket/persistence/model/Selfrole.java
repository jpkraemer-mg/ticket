package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Selfrole {
  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private Long roleId;

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_CATEGORY_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;
}
