package quest.darkoro.ticket.listener.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.DataUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

  private final DataUtil dataUtil;

  public void handleQuery(SlashCommandInteractionEvent e) {
    var profile = dataUtil.fetchProfile(e.getOption("username").getAsString());
    if (profile == null) {
      e.reply("Minecraft API did not return any data for username %s".formatted(
          e.getOption("username").getAsString())).queue();
      return;
    }
    e.reply("UUID: %s".formatted(dataUtil.fixUUID(profile.get("id").toString()))).queue();
  }
}
