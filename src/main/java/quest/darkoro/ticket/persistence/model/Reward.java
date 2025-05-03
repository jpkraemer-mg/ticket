package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class Reward {
  @Id
  @GeneratedValue(generator = "UUID")
  private UUID id;

  @Column
  private String name;

  @ManyToOne
  private RewardTier tier;

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_REWARD_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;

  @Column
  private LocalDateTime createdAt = LocalDateTime.now();
}
