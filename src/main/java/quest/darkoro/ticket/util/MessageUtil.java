package quest.darkoro.ticket.util;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.DANGER;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.SECONDARY;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.SUCCESS;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.persistence.CategoryRepository;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.persistence.SelfroleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUtil {

  private final CategoryRepository categoryRepository;
  private final SelfroleRepository selfroleRepository;
  private final GuildRepository guildRepository;

  public void sendTicketMessage(TextChannel channel, JDA bot) {
    if (channel == null) {
      log.warn("Configured ticket channel no longer exists, skipping ticket message");
      return;
    }
    var embed = new EmbedBuilder()
        .setTitle("Support tickets")
        .setDescription(
            "Report bugs you find on the server or request assistance with any other topic!\nPlease remember to be honest with all reports!\nTo create a ticket, use the button below.")
        .setColor(Color.GREEN)
        .setFooter("DB0S Ticket & Support System - %s categories available".formatted(
            categoryRepository.findByGuildId(channel.getGuild().getIdLong()).size()))
        .build();

    channel.getIterableHistory().takeAsync(50).thenAccept(messages -> {
      var tasks = messages.stream()
          .filter(m -> m.getAuthor().equals(bot.getSelfUser()))
          .filter(m -> !m.getEmbeds().isEmpty() && m.getEmbeds().stream()
              .anyMatch(e -> "Support tickets".equals(e.getTitle()))
          )
          .map(m -> m.delete().submit())
          .toList();

      CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).thenRun(() -> {
        try {
          channel.sendMessageEmbeds(embed)
              .addActionRow(
                  Button.of(PRIMARY, "ticket_create", "Create ticket", Emoji.fromUnicode("📩"))
              )
              .queue();
        } catch (MissingAccessException ex) {
          log.error("Cannot send message to channel {}: {}", channel.getId(), ex.getMessage());
        } catch (Exception ex) {
          log.error("Unexpected error while sending message to channel {}: {}", channel.getId(),
              ex.getMessage());
        }
      });
    });
  }

  public void sendRoleMessage(TextChannel channel, JDA bot) {
    if (channel == null) {
      log.warn("Configured selfrole channel no longer exists, skipping role message");
      return;
    }
    var roles = selfroleRepository.findByGuildId(channel.getGuild().getIdLong());
    var embed = new EmbedBuilder()
        .setTitle("Self-assignable roles")
        .setDescription(
            "This is a list of all self-assignable roles available on the server.\nTo apply a role, use the buttons below.")
        .setColor(Color.GREEN)
        .setFooter("DB0S Ticket & Support System - %s roles available".formatted(roles.size()))
        .build();

    channel.getIterableHistory().takeAsync(50).thenAccept(messages -> {
      var tasks = messages.stream()
          .filter(m -> m.getAuthor().equals(bot.getSelfUser()))
          .filter(m -> !m.getEmbeds().isEmpty() && m.getEmbeds().stream()
              .anyMatch(e -> "Self-assignable roles".equals(e.getTitle()))
          )
          .map(m -> m.delete().submit())
          .toList();

      CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).thenRun(() -> {
        try {
          var msg = new MessageCreateBuilder()
              .addContent("")
              .addEmbeds(embed);
          var valid = roles.stream().map(r -> bot.getRoleById(r.getRoleId()))
              .filter(Objects::nonNull).toList();
          var colors = List.of(PRIMARY, DANGER, SUCCESS, SECONDARY);
          var buttons = new ArrayList<Button>();
          for (var v : valid) {
            var color = colors.get(valid.indexOf(v) % colors.size());
            buttons.add(Button.of(color, "selfrole_" + v.getId(), v.getName()));
            if (buttons.size() == 5) {
              msg.addActionRow(buttons);
              buttons.clear();
            }
          }
          if (!buttons.isEmpty()) msg.addActionRow(buttons);
          channel.sendMessage(msg.build()).queue();
        } catch (MissingAccessException ex) {
          log.error("Cannot send message to channel {}: {}", channel.getId(), ex.getMessage());
        } catch (Exception ex) {
          log.error("Unexpected error while sending message to channel {}: {}", channel.getId(),
              ex.getMessage());
        }
      });
    });
  }

  public void sendLogMessage(String message, TextChannel channel) {
    if (channel == null) {
      log.warn("Configured log channel no longer exists, skipping log message: {}", message);
      return;
    }
    channel.sendMessage(message).queue();
  }

  public void sendGuildLog(Guild guild, String message) {
    guildRepository.findById(guild.getIdLong())
        .map(g -> g.getLog())
        .ifPresent(logId -> sendLogMessage(message, guild.getTextChannelById(logId)));
  }

  public void sendCommandLog(Member member, String command, String details) {
    sendGuildLog(member.getGuild(),
        "Command `%s` executed by `%s (%s)`\n%s".formatted(
            command, member.getEffectiveName(), member.getIdLong(), details));
  }

  public void refreshTicketMessage(Guild guild, JDA bot) {
    guildRepository.findById(guild.getIdLong())
        .map(g -> g.getBase())
        .ifPresent(base -> sendTicketMessage(guild.getTextChannelById(base), bot));
  }

  public void refreshRoleMessage(Guild guild, JDA bot) {
    guildRepository.findById(guild.getIdLong())
        .map(g -> g.getRole())
        .ifPresent(role -> sendRoleMessage(guild.getTextChannelById(role), bot));
  }

}
