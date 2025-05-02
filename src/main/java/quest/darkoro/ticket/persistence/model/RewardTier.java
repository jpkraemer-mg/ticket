package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class RewardTier {
  @Id
  @GeneratedValue(generator = "UUID")
  private UUID id;

  @Column
  private String name;

  @OneToMany(mappedBy = "tier")
  private Set<Reward> rewards = new HashSet<>();

  @JoinColumn(name = "guildId", foreignKey = @ForeignKey(name = "FK_REWARDTIER_GUILD", foreignKeyDefinition = "FOREIGN KEY (guild_id) REFERENCES guild(id)"))
  private Long guildId;
}
