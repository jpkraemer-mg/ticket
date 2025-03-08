package quest.darkoro.ticket.services;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.DANGER;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;
import quest.darkoro.transcripts.DiscordHtmlTranscripts;

@Slf4j
@RequiredArgsConstructor
@Service
public class ButtonService {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;
  private final CategoryRepository categoryRepository;

  public void distributeEvent(ButtonInteractionEvent e) {
    switch (e.getButton().getId()) {
      case "close_ticket" -> handleTicketClose(e);
      case "delete_ticket" -> handleTicketDelete(e);
      case "resolve_ticket" -> handleResolveTicket(e);
      case "ticket_create" -> handleTicketCreate(e);
      case "transcript" -> handleTicketTranscript(e);
      default -> {
        if (e.getButton().getId().startsWith("role_")) {
          configureSelfroleRoleId(e);
        } else {
          e.reply("Unknown button!").setEphemeral(true).queue();
        }
      }
    }
  }

  public void configureSelfroleRoleId(ButtonInteractionEvent e) {
    if (!e.getGuild().getSelfMember().canInteract(e.getMember())) {
      e.reply(
              "I can't add or remove this role from you!\nThis most likely is because you have a higher role than me!")
          .setEphemeral(true).queue();
      return;
    }
    var rid = e.getButton().getId().substring(e.getButton().getId().lastIndexOf('_') + 1);
    var role = e.getGuild().getRoleById(rid);
    if (role == null) {
      e.reply("This role does not exist! Please open a ticket and contact an Admin about this!")
          .setEphemeral(true).queue();
      return;
    }
    if (e.getMember().getRoles().contains(role)) {
      e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
    } else {
      e.getGuild().addRoleToMember(e.getMember(), role).queue();
    }
    e.reply("Role " + role.getAsMention() + " has been " + (!e.getMember().getRoles().contains(role)
        ? "added" : "removed") + "!").setEphemeral(true).queue();
  }

  public void handleTicketClose(ButtonInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getChannel().asTextChannel();
      var embed = new EmbedBuilder().setDescription(
          "```DO NOT DELETE NON-DUPLICATE / NON-SUPPORT TICKETS WITHOUT TRANSCRIPT!```").build();
      channel.getMemberPermissionOverrides().forEach(p -> p.delete().queue());
      e
          .editMessage(e.getMessage().getContentRaw())
          .setEmbeds(e.getMessage().getEmbeds())
          .setActionRow(
              e.getMessage().getButtonById("close_ticket").asDisabled(),
              e.getMessage().getButtonById("resolve_ticket").asDisabled()
          )
          .queue();
      e.getChannel()
          .asTextChannel()
          .sendMessage("Ticket closed by **%s** and all non-role members removed."
              .formatted(member.getEffectiveName()))
          .addEmbeds(embed)
          .addActionRow(
              Button.of(DANGER, "delete_ticket", "DELETE", Emoji.fromUnicode("\uD83D\uDDD1\uFE0F")),
              Button.of(PRIMARY, "transcript", "TRANSCRIPT", Emoji.fromUnicode("\uD83D\uDCDD"))
          )
          .queue();
    }
  }

  public void handleTicketDelete(ButtonInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getInteraction().getChannel();
      e.reply("Ticket will be deleted in 5 seconds.")
          .queue(c -> c.getInteraction().getChannel().delete().queueAfter(5, TimeUnit.SECONDS));

      var guild = guildRepository.findById(gid).orElse(null);
      if (guild != null) {
        if (guild.getLog() != null) {
          messageUtil.sendLogMessage(
              "Ticket deleted - `%s (%s)`\nDeleted by `%s (%s)`".formatted(
                  channel.getName(),
                  channel.getIdLong(),
                  member.getEffectiveName(),
                  member.getIdLong()
              ), e.getGuild().getTextChannelById(guild.getLog())
          );
        }
      }
    }
  }

  public void handleResolveTicket(ButtonInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var menu = StringSelectMenu.create("resolve_bug")
          .addOption("Resolved / Other", "t0")
          .addOption("Tier 1", "t1")
          .addOption("Tier 2", "t2")
          .addOption("Tier 3", "t3")
          .build();
      e.editButton(e.getButton().withDisabled(true)).queue();
      e.getChannel().asTextChannel().sendMessage("").setActionRow(menu).queue();
    }
  }

  public void handleTicketCreate(ButtonInteractionEvent e) {
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

  public void handleTicketTranscript(ButtonInteractionEvent e) {
    var guild = e.getGuild();
    var gid = guild.getIdLong();

    var transcriptExist = guildRepository.findById(gid).isPresent();
    if (!transcriptExist) {
      e.reply("No configuration available!\nSet one using `/configure transcript`")
          .setEphemeral(true).queue();
      return;
    }

    TextChannel transcript;
    var g = guildRepository.findById(gid).orElse(null);
    if (g != null && g.getTranscript() != null) {
      transcript = guild.getTextChannelById(guildRepository.findById(gid).get().getTranscript());
      if (transcript == null) {
        e.reply("Transcript channel not found!").setEphemeral(true).queue();
        return;
      }
    } else {
      e.reply("Transcript channel not found!").setEphemeral(true).queue();
      return;
    }

    var channel = e.getChannel().asTextChannel();

    try {
      transcript.sendFiles(DiscordHtmlTranscripts.getInstance()
              .createTranscript(channel,
                  "transcript_%s.html".formatted(channel.getName().toLowerCase())))
          .queue();
    } catch (IOException ex) {
      e.reply("Error while creating transcript from channel '%s'".formatted(channel.getName()))
          .queue();
      log.error("Error while creating transcript from channel '{}'", channel.getName(), ex);
      return;
    } catch (MissingAccessException ex) {
      e.reply("Missing access to transcript channel '%s'"
              .formatted(transcript.getName()))
          .setEphemeral(true)
          .queue();
      log.error("Missing access to transcript channel '{}'", transcript.getName(), ex);
      return;
    }

    e.reply("Transcript saved to %s!".formatted(transcript.getAsMention())).queue();
    if (g.getLog() != null) {
      messageUtil.sendLogMessage(
          "Button `%s` executed by `%s (%s)`\nSAVE TRANSCRIPT: `%s (%s)`".formatted(
              e.getButton().getLabel(),
              e.getMember().getEffectiveName(),
              e.getMember().getIdLong(),
              channel.getName(),
              channel.getIdLong()), e.getGuild().getTextChannelById(g.getLog())
      );
    }
  }
}
