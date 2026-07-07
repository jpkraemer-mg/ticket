package quest.darkoro.ticket.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.DataUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryCommandService {

  private final DataUtil dataUtil;

  public void handleQuery(SlashCommandInteractionEvent e) {
    var username = e.getOption("username").getAsString();
    e.deferReply().queue();
    var profile = dataUtil.fetchProfile(username);
    if (profile == null) {
      e.getHook().sendMessage("Minecraft API did not return any data for username %s"
          .formatted(username)).queue();
      return;
    }
    e.getHook().sendMessage("UUID: %s".formatted(dataUtil.fixUUID(profile.get("id").toString())))
        .queue();
  }
}
