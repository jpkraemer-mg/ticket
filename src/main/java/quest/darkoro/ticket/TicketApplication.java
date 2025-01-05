package quest.darkoro.ticket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TicketApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketApplication.class, args);
    log.info("Application running");
  }

}
