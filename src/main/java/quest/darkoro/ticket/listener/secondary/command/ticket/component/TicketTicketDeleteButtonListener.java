package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@SecondaryListener
@Service
@Slf4j
@RequiredArgsConstructor
public class TicketTicketDeleteButtonListener extends ListenerAdapter {

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("ticket_delete")) {
      return;
    }
    e.reply("Ticket will be deleted in 5 seconds.")
        .setActionRow(e.getButton().asDisabled())
        .queue(c -> c.getInteraction().getChannel().delete().queueAfter(5, TimeUnit.SECONDS));
  }
}
