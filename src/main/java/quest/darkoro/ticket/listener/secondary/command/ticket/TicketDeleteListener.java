package quest.darkoro.ticket.listener.secondary.command.ticket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
@SecondaryListener
public class TicketDeleteListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") || !"delete".equals(
        e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      e.reply("Are you sure about deleting this ticket?")
          .addActionRow(
              Button.success("ticket_delete", "Confirm"),
              Button.danger("ticket_no_delete", "Deny")
          )
          .queue();
    }
  }
}
