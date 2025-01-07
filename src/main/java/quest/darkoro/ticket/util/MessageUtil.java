package quest.darkoro.ticket.util;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;

import java.awt.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUtil {

  private final CategoryRepository categoryRepository;
  private final JDA bot;

  public void sendTicketMessage(TextChannel channel) {
    channel.getIterableHistory().takeAsync(100).thenAccept(messages -> {
      messages.stream()
          .filter(m -> m.getAuthor().equals(bot.getSelfUser()))
          .forEach(m -> m.delete().queue());
    });
    var embed = new EmbedBuilder()
        .setTitle("Support tickets")
        .setDescription("Report bugs you find on the server or request assistance with any other topic!\nPlease remember to be honest with all reports!\nTo create a ticket, use the button below.")
        .setColor(Color.GREEN)
        .setFooter("DB0S Ticket & Support System - %s categories available".formatted(categoryRepository.findByGuildId(channel.getGuild().getIdLong()).size()))
        .build();
    try {
      channel.sendMessageEmbeds(embed)
          .addActionRow(
              Button.of(PRIMARY, "ticket_create", "Create ticket", Emoji.fromUnicode("📩"))
          )
          .queue();
    } catch (MissingAccessException ex) {
      log.error("Cannot send message to channel {}: {}", channel.getId(), ex.getMessage());
    } catch (Exception ex) {
      log.error("Unexpected error while sending message to channel {}: {}", channel.getId(), ex.getMessage());
    }
  }

}
