package quest.darkoro.ticket.services;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Ticket;
import quest.darkoro.ticket.persistence.CategoryRepository;
import quest.darkoro.ticket.persistence.TicketRepository;
import quest.darkoro.ticket.util.DataUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModalService {

  private final DataUtil dataUtil;
  private final CategoryRepository categoryRepository;
  private final PermissionUtil permissionUtil;
  private final TicketRepository ticketRepository;

  public void distributeEvent(ModalInteractionEvent e) {
    if (e.getModalId().startsWith("ticket_create")) {
      handleTicketCreate(e);
    } else {
      e.reply("Unknown modal: %s".formatted(e.getModalId())).setEphemeral(true).queue();
    }
  }

  private void handleTicketCreate(ModalInteractionEvent e) {
    e.deferReply(true).queue();

    var selected = e.getModalId().substring(e.getModalId().lastIndexOf("_") + 1).toUpperCase();

    var builder = new EmbedBuilder().setTitle(selected);

    e.getValues()
        .stream()
        .filter(v -> !v.getAsString().isEmpty())
        .filter(v -> !v.getId().equals("title"))
        .forEach(v -> {
          if (v.getId().equals("name")) {
            var profile = dataUtil.fetchProfile(v.getAsString());
            String name = null;
            String uuid = null;
            if (profile != null) {
              name = profile.get("name").toString();
              uuid = dataUtil.fixUUID(profile.get("id").toString());
            }
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
              builder.addField("TIER", "```Tier 1 - Incorrect Tier %s submitted```".formatted(val),
                  false);
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

    ticketRepository.save(
        new Ticket()
            .setCreator(e.getMember().getIdLong())
            .setTitle(e.getValue("title").getAsString())
            .setDescription(e.getValue("problem").getAsString())
            .setGuildId(guild.getIdLong())
            .setChannel(channel.getIdLong())
    );

    channel.sendMessage(
            ("""
                || %s ||
                %s, thank you for opening a ticket! We've been pinged and someone will respond soon.
                You can ping a member of staff if there's been no response for 48 hours.
                
                Please submit any additional evidence you may have in case it might be needed to help you!
                """)
                .formatted(cat.getMentions(), e.getMember().getAsMention())
        )
        .addEmbeds(embed)
        .addActionRow(
            Button.of(ButtonStyle.DANGER, "close_ticket", "CLOSE",
                Emoji.fromUnicode("\uD83D\uDD12")),
            Button.of(ButtonStyle.PRIMARY, "resolve_ticket", "RESOLVE", Emoji.fromUnicode("✨"))
        )
        .queue();
    e.getHook().sendMessage("Ticket created").queue();
  }
}
