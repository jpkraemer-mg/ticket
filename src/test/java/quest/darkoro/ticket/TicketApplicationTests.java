package quest.darkoro.ticket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires a running MySQL instance and a Discord token; never executed before "
    + "useJUnitPlatform() was enabled. Run manually against a real environment.")
@SpringBootTest
class TicketApplicationTests {

  @Test
  void contextLoads() {
  }

}
