package quest.darkoro.ticket.listener.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Administrator;
import quest.darkoro.ticket.persistence.repository.AdministratorRepository;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@RequiredArgsConstructor
@Slf4j
public class TicketAdminsAddCommandListener extends ListenerAdapter {

  private final AdministratorRepository administratorRepository;
  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") ||
        !"admins".equals(e.getSubcommandGroup()) || !"add".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var role = e.getOption("role").getAsRole();
      administratorRepository.save(
          new Administrator().setGuildId(gid).setRoleId(role.getIdLong()).setName(role.getName()));
      e.reply("Role %s added to ticket admins.".formatted(role.getAsMention())).setEphemeral(true)
          .queue();
    }
  }
}
