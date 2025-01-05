package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.PARAGRAPH;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class TicketCreateSelectListener extends ListenerAdapter {

  @Override
  public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent e) {
    if (e.isAcknowledged() || !e.getComponentId().equals("ticket_select")) {
      return;
    }

    var selected = e.getSelectedOptions().get(0).getLabel();

    TextInput title = TextInput.create("title", "Title of the ticket", SHORT)
        .setPlaceholder("Please put a short and precise title for this ticket")
        .setRequired(true)
        .setMinLength(5)
        .setMaxLength(45)
        .build();

    TextInput tier = TextInput.create("tier", "Tier of Bug", SHORT)
        .setPlaceholder("What Tier is this bug? [1 | 2 | 3] - 0 if no bug")
        .setRequired(false)
        .setMaxLength(1)
        .build();

    TextInput name = TextInput.create("name", "What is your IGN?", SHORT)
        .setPlaceholder("Please put your ingame name from the server here.")
        .setMinLength(3)
        .setMaxLength(25)
        .setRequired(true)
        .build();

    TextInput problem = TextInput.create("problem", "Describe your problem", PARAGRAPH)
        .setPlaceholder(
            "Please describe your problem as precisely as possible so that we can best help you.")
        .setRequired(true)
        .build();

    Modal modal = Modal.create("ticket_create%s".formatted("_" + selected.toLowerCase()),
            selected + " Ticket")
        .addActionRow(title)
        .addActionRow(name)
        .addActionRow(problem)
        .addActionRow(tier)
        .build();

    e.replyModal(modal).queue();
  }
}
