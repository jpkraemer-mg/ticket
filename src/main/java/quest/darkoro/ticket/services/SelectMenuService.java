package quest.darkoro.ticket.services;

import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.PARAGRAPH;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Selfrole;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.persistence.repository.SelfroleRepository;
import quest.darkoro.ticket.persistence.repository.TicketRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class SelectMenuService {

  private final SelfroleRepository selfroleRepository;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;
  private final PermissionUtil permissionUtil;
  private final TicketRepository ticketRepository;

  public void distributeEvent(GenericSelectMenuInteractionEvent<?, ?> e) {
    if (e instanceof StringSelectInteractionEvent) {
      switch (e.getComponentId()) {
        case "ticket_select" -> handleTicketCreate((StringSelectInteractionEvent) e);
        case "resolve_bug" -> handleTicketResolveBug((StringSelectInteractionEvent) e);
        default -> e.reply("Unknown StringSelectInteractionEvent: %s".formatted(e.getComponentId()))
            .setEphemeral(true).queue();
      }
    } else if (e instanceof EntitySelectInteractionEvent) {
      switch (e.getComponentId()) {
        case "selfrole_add" -> handleConfigureSelfroleAdd((EntitySelectInteractionEvent) e);
        case "selfrole_remove" -> handleConfigureSelfroleRemove((EntitySelectInteractionEvent) e);
        default -> e.reply("Unknown EntitySelectInteractionEvent: %s".formatted(e.getComponentId()))
            .setEphemeral(true).queue();
      }
    } else {
      throw new IllegalArgumentException("Unknown event type: " + e.getClass().getName());
    }
  }

  public void handleConfigureSelfroleAdd(EntitySelectInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var existing = selfroleRepository
        .findByGuildId(gid)
        .stream()
        .map(Selfrole::getRoleId)
        .collect(Collectors.toSet());

    var roles = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> !existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .map(Role::getName)
        .toList();

    var added = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> !existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .peek(r -> selfroleRepository.save(
            new Selfrole().setGuildId(gid).setRoleId(r.getIdLong()))
        )
        .map(Role::getAsMention)
        .toList();

    e.reply("Added the following roles to self-assignable roles:\n%s".formatted(
        String.join("\n", added))).setEphemeral(true).queue();
    var guild = guildRepository.findById(gid).orElse(null);
    if (guild != null) {
      if (guild.getRole() != null) {
        messageUtil.sendRoleMessage(e.getGuild().getTextChannelById(guild.getRole()), e.getJDA());
      }
      if (guild.getLog() != null) {
        messageUtil.sendLogMessage(
            "Command `%s` executed by `%s (%s)`\nSELF-ASSIGNABLE ROLE(S) ADD `%s`".formatted(
                "/configure selfrole add",
                member.getEffectiveName(),
                member.getIdLong(),
                roles
            ), e.getGuild().getTextChannelById(guild.getLog()));
      }
    }
  }

  public void handleConfigureSelfroleRemove(EntitySelectInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var existing = selfroleRepository
        .findByGuildId(gid)
        .stream()
        .map(Selfrole::getRoleId)
        .collect(Collectors.toSet());

    var roles = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .map(Role::getName)
        .toList();

    var removed = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .peek(r -> selfroleRepository.deleteByGuildIdAndRoleId(gid, r.getIdLong()))
        .map(Role::getAsMention)
        .toList();

    e.reply("Removed the following roles from self-assignable roles:\n%s".formatted(
        String.join("\n", removed))).setEphemeral(true).queue();
    var guild = guildRepository.findById(gid).orElse(null);
    if (guild != null) {
      if (guild.getRole() != null) {
        messageUtil.sendRoleMessage(e.getGuild().getTextChannelById(guild.getRole()), e.getJDA());
      }
      if (guild.getLog() != null) {
        messageUtil.sendLogMessage(
            "Command `%s` executed by `%s (%s)`\nSELF-ASSIGNABLE ROLE(S) REMOVE `%s`".formatted(
                "/configure selfrole remove",
                member.getEffectiveName(),
                member.getIdLong(),
                roles
            ), e.getGuild().getTextChannelById(guild.getLog()));
      }
    }
  }

  public void handleTicketCreate(StringSelectInteractionEvent e) {
    var selected = e.getSelectedOptions().get(0).getLabel();

    TextInput title = TextInput.create("title", "Title of the ticket", SHORT)
        .setPlaceholder("Please put a short and precise title for this ticket")
        .setRequired(true)
        .setMinLength(5)
        .setMaxLength(45)
        .build();

    TextInput tier = TextInput.create("tier", "Tier of Bug", SHORT)
        .setPlaceholder("What Tier is this bug? [1 | 2 | 3] - KEEP EMPTY IF NO BUG!")
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
        .setMaxLength(800)
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

  public void handleTicketResolveBug(StringSelectInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var selected = e.getSelectedOptions().get(0).getValue();

      e.getMessage().delete().queue();

      switch (selected) {
        case "t0" ->
            e.reply("User won't get pinged, no reward to be given out.").setEphemeral(true).queue();
        case "t1" -> {
          e.reply("User will be asked to choose any one T1 reward").setEphemeral(true).queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage("%s, you may choose any one reward from **T1 Bug Report Rewards**"
                  .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete()
                      .getAsMention())
              )
              .queue();
        }
        case "t2" -> {
          e.reply("User will be asked to choose any one T1 or T2 reward").setEphemeral(true)
              .queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage("%s, you may choose any one reward from **T1 or T2 Bug Report Rewards**"
                  .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete()
                      .getAsMention())
              )
              .queue();
        }
        case "t3" -> {
          e.reply("User will be asked to choose any one reward").setEphemeral(true).queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage(
                  "%s, you may choose any one reward from **T1, T2 or T3 Bug Report Rewards**"
                      .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete()
                          .getAsMention())
              )
              .queue();
        }
      }
    }
  }
}
