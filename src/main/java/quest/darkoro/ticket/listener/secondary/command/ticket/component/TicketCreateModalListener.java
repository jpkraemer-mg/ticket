package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.util.DataUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@SecondaryListener
@RequiredArgsConstructor
public class TicketCreateModalListener extends ListenerAdapter {

  private final CategoryRepository categoryRepository;
  private final PermissionUtil permissionUtil;
  private final DataUtil dataUtil;

  @Override
  public void onModalInteraction(@NonNull ModalInteractionEvent e) {
    if (e.isAcknowledged() || !e.getModalId().startsWith("ticket_create")) {
      return;
    }

    var selected = e.getModalId().substring(e.getModalId().lastIndexOf("_") + 1).toUpperCase();

    var builder = new EmbedBuilder().setTitle(selected);

    e.getValues()
        .stream()
        .filter(v -> !v.getAsString().isEmpty())
        .filter(v -> !v.getId().equals("title"))
        .forEach(v -> {
          if (v.getId().equals("name")) {
            var name = dataUtil.fetchCorrectUsername(v.getAsString());
            var uuid = dataUtil.fetchUUID(v.getAsString());
            builder.addField("USERNAME",
                "```%s```".formatted(Objects.requireNonNullElseGet(name, v::getAsString)), false);
            if (uuid != null) {
              builder.addField("UUID", "```%s```".formatted(uuid), false);
            }
          } else if (v.getId().equals("tier")) {
            var val = v.getAsString();
            if (val.matches("[123]")) {
              builder.addField("TIER", "```Tier %s```".formatted(val), false);
            } else if (val.equals("0")) {
            } else {
              builder.addField("TIER", "```Tier 1 - Incorrect Tier %s submitted```".formatted(val), false);
            }
          } else {
            builder.addField(v.getId().replaceAll("[\\[\\]\\\\_/\\-]", " ").toUpperCase(),
                "```%s```".formatted(v.getAsString()), false);
          }
        });

    var embed = builder.build();

    var guild = e.getGuild();
    var cat = categoryRepository.findByNameAndGuildId(selected, guild.getIdLong());
    var category = guild.getCategoryById(cat.getId());
    var channel = guild.createTextChannel("%s".formatted(e.getValue("title").getAsString()),
            category)
        .setParent(category)
        .syncPermissionOverrides()
        .addMemberPermissionOverride(e.getMember().getIdLong(), permissionUtil.getAllow(),
            permissionUtil.getDeny())
        .complete();
    channel.sendMessage(
        ("""
            ||%s||
            %s, thank you for opening a ticket! We've been pinged and someone will respond soon.
            You can ping a member of staff if there's been no response for 48 hours.
            
            Please submit any additional evidence you may have in case it might be needed to help you!
            """)
            .formatted(cat.getMentions(), e.getMember().getAsMention())
    ).addEmbeds(embed).queue();

    e.reply("Ticket created").setEphemeral(true).queue();
  }
}
