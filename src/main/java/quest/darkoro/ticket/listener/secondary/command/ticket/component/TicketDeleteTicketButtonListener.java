package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class TicketDeleteTicketButtonListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("delete_ticket")) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      e.reply("Are you sure about deleting this ticket?")
          .addActionRow(
              Button.success("ticket_delete", "Confirm"),
              Button.danger("ticket_no_delete", "Deny")
          )
          .setEphemeral(true)
          .queue();
    }

  }
}
