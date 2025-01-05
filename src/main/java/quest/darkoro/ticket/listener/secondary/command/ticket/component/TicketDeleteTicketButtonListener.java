package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
      e.reply("Ticket will be deleted in 5 seconds.")
          .queue(c -> c.getInteraction().getChannel().delete().queueAfter(5, TimeUnit.SECONDS));
    }

  }
}
