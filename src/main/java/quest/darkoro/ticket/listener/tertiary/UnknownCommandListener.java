package quest.darkoro.ticket.listener.tertiary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.TertiaryListener;

@TertiaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class UnknownCommandListener extends ListenerAdapter {

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged()) {
      return;
    }
    e.reply("Unknown (sub)command").setEphemeral(true).queue();
  }
}
