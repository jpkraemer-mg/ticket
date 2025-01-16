package quest.darkoro.ticket.listener.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.AdministratorRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@RequiredArgsConstructor
@Slf4j
public class TicketAdminsRemoveCommandListener extends ListenerAdapter {

  private final AdministratorRepository administratorRepository;
  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") ||
        !"admins".equals(e.getSubcommandGroup()) || !"remove".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var role = e.getOption("role").getAsRole();
      administratorRepository.removeByRoleId(role.getIdLong());
      e.reply("Role %s removed from ticket admins.".formatted(role.getAsMention()))
          .setEphemeral(true).queue();

      var guild = guildRepository.findById(gid).orElse(null);
      if (guild != null) {
        if (guild.getLog() != null) {
          messageUtil.sendLogMessage(
              "Command `%s` executed by `%s (%s)`\nREMOVE TICKET ADMIN: `%s (%s)`".formatted(
                  "/ticket admins remove",
                  e.getMember().getEffectiveName(),
                  e.getMember().getIdLong(),
                  role.getName(),
                  role.getId()), e.getGuild().getTextChannelById(guild.getLog())
          );
        }
      }
    }
  }
}
