package quest.darkoro.ticket.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
public class Guild {

  @Id
  private Long id;

  @Column
  private Long transcript;

  @Column
  private Long base;

  @Column
  private Long role;

  @Column
  private Long log;
}
