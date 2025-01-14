package quest.darkoro.ticket.listener.secondary.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@SecondaryListener
@RequiredArgsConstructor
public class ConfigureChannelRoleCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("configure") || !"channel".equals(
        e.getSubcommandGroup()) || !"roles".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setRole(e.getOption("roles").getAsRole().getIdLong()));
      e.reply("The channel from which to self-assign roles has been set to %s".formatted(e.getOption("roles").getAsRole().getAsMention())).setEphemeral(true).queue();
      messageUtil.sendRoleMessage(e.getOption("roles").getAsChannel().asTextChannel(), e.getJDA());
    }
  }
}
