package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class TicketTranscriptButtonListener extends ListenerAdapter {
  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("transcript")) {
      return;
    }
    e.reply("Can't transcript, no function").setEphemeral(true).queue();
  }
}
