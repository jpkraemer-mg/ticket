package quest.darkoro.ticket.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.annotations.TertiaryListener;

@RequiredArgsConstructor
@Service
@Slf4j
public class BotReadyListener extends ListenerAdapter {

  private final ApplicationContext applicationContext;

  @Override
  public void onReady(ReadyEvent e) {
    var bot = e.getJDA();
    var annotations = List.of(PrimaryListener.class, SecondaryListener.class,
        TertiaryListener.class);
    annotations.forEach(a -> {
      applicationContext.getBeansWithAnnotation(a)
          .values()
          .forEach(bot::addEventListener);
      log.info("Registered {} successfully: {}",
          a.getSimpleName(),
          applicationContext.getBeansWithAnnotation(a)
              .values()
              .stream()
              .map(l -> l.getClass().getSimpleName())
              .toList()
      );
    });
    log.info("Bot ready and running on {} Guild(s).", bot.getGuilds().size());
  }
}
