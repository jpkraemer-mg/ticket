package quest.darkoro.ticket.listener.secondary.command.ticket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
@SecondaryListener
@Slf4j
public class TicketCreateListener extends ListenerAdapter {

  private final CategoryRepository categoryRepository;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") || !"create".equals(
        e.getSubcommandName())) {
      return;
    }
    if (categoryRepository.findByGuildId(e.getGuild().getIdLong()).isEmpty()) {
      e.reply(
              "You must set up at least one ticket category.\nUse `/configure category add` for this.")
          .setEphemeral(true).queue();
      return;
    }
    var builder = StringSelectMenu.create("ticket_select").setPlaceholder("Select ticket category");
    categoryRepository.findAll().forEach(c ->
        builder.addOption(c.getName().toUpperCase(), c.getName(), c.getDescription())
    );
    var menu = builder.build();
    e.reply("").addActionRow(menu).setEphemeral(true).queue();
  }
}
