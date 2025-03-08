package quest.darkoro.ticket.listeners.secondary.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketTicketCreateButtonListener extends ListenerAdapter {

  private final CategoryRepository categoryRepository;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("ticket_create")) {
      return;
    }

    var gid = e.getGuild().getIdLong();

    if (categoryRepository.findByGuildId(gid).isEmpty()) {
      e.reply(
              "You must set up at least one ticket category.\nUse `/configure category add` for this.")
          .setEphemeral(true).queue();
      return;
    }
    var builder = StringSelectMenu.create("ticket_select").setPlaceholder("Select ticket category");
    categoryRepository.findByGuildId(gid).forEach(c ->
        builder.addOption(c.getName().toUpperCase(), c.getName(), c.getDescription())
    );
    var menu = builder.build();
    e.reply("").addActionRow(menu).setEphemeral(true).queue();
  }
}
