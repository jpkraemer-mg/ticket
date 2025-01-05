package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class TicketTicketNoDeleteButtonListener extends ListenerAdapter {

  @Override
  public void onButtonInteraction(ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("ticket_no_delete")) {
      return;
    }
    e.editMessage(e.getMessage().getContentRaw()).setActionRow(e.getButton().asDisabled()).queue();
  }
}
