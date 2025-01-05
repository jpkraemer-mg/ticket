package quest.darkoro.ticket.listener.secondary.command.configure;

import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ryzeon.transcripts.DiscordHtmlTranscripts;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class ConfigureTranscriptCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("configure") || !"transcript".equals(
        e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      guildRepository.save(new Guild().setId(gid).setTranscript(e.getOption("transcript").getAsChannel().getIdLong()));
      e.reply("Transcript channel set to %s".formatted(e.getOption("transcript").getAsChannel().getAsMention())).setEphemeral(true).queue();
    }
  }
}
