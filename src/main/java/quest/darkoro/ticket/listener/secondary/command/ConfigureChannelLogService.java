package quest.darkoro.ticket.listener.secondary.command;

import static net.dv8tion.jda.api.entities.channel.ChannelType.TEXT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigureChannelLogService {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;

  public void handleConfigureChannelLog(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("log").getAsChannel();
      if (channel.getType() != TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setLog(channel.getIdLong()));
      e.reply("Log channel set to %s".formatted(channel.getAsMention())).setEphemeral(true).queue();
    }

  }
}
