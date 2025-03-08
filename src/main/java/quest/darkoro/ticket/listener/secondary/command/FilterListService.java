package quest.darkoro.ticket.listener.secondary.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class FilterListService extends ListenerAdapter {

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("filter") || !"list".equals(
        e.getSubcommandName())) {
      return;
    }
  }
}
