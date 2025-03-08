package quest.darkoro.ticket.listeners.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigureChannelTranscriptService {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  public void handleConfigureChannelTranscript(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("transcript").getAsChannel();
      if (channel.getType() != ChannelType.TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setTranscript(channel.getIdLong()));
      e.reply("Transcript channel set to %s".formatted(channel.getAsMention())).setEphemeral(true)
          .queue();
      if (guild.getLog() != null) {
        messageUtil.sendLogMessage(
            "Command `%s` executed by `%s (%s)`\nCONFIGURE TRANSCRIPT CHANNEL: `%s (%s)`".formatted(
                "/configure channel transcript",
                member.getEffectiveName(),
                member.getIdLong(),
                channel.getName(),
                channel.getId()), e.getGuild().getTextChannelById(guild.getLog())
        );
      }
    }
  }
}
