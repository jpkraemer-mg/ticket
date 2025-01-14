package quest.darkoro.ticket.listener.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class AllCommandsListener extends ListenerAdapter {

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged()) {
      return;
    }
    if (e.getGuild() != null) {
      e.reply("This command may only be used in a guild!").setEphemeral(true).queue();
    }
  }
}
