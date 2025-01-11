package quest.darkoro.ticket.listener.secondary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.DataUtil;

@Service
@Slf4j
@RequiredArgsConstructor
@SecondaryListener
public class QueryCommandListener extends ListenerAdapter {

  private final DataUtil dataUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("query")) {
      return;
    }
    var profile = dataUtil.fetchProfile(e.getOption("username").getAsString());
    if (profile == null) {
      e.reply("Minecraft API did not return any data for username %s".formatted(e.getOption("username").getAsString())).queue();
      return;
    }
    e.reply("UUID: %s".formatted(profile.get("uuid").toString())).queue();
  }
}
