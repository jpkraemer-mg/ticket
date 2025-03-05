package quest.darkoro.ticket.listener.tertiary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.TertiaryListener;

@TertiaryListener
@RequiredArgsConstructor
@Slf4j
@Service
public class SlashCommandInteractionListener extends ListenerAdapter {
  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged()) {
      return;
    }
    log.debug("Test");
  }
}
