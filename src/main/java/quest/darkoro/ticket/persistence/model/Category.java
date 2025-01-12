package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Entity
@Accessors(chain = true)
public class Category {

  @Id
  private Long id;

  @Column
  private String name;

  @Column
  private String description;

  @Column
  private String mentions;

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_CATEGORY_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;

  @Column
  private Integer count = 0;
}
