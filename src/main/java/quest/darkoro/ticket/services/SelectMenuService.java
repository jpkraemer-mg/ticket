package quest.darkoro.ticket.services;

import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.PARAGRAPH;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;

import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.RewardRepository;
import quest.darkoro.ticket.persistence.RewardTierRepository;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.model.Selfrole;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.persistence.SelfroleRepository;
import quest.darkoro.ticket.persistence.TicketRepository;
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
  private final RewardRepository rewardRepository;
  private final RewardTierRepository rewardTierRepository;

  public void distributeEvent(GenericSelectMenuInteractionEvent<?, ?> e) {
    if (e instanceof StringSelectInteractionEvent ev) {
      switch (e.getComponentId()) {
        case "ticket_select" -> handleTicketCreate(ev);
        case "resolve_bug" -> handleTicketResolveBug(ev);
        case "deletechoosetier" -> handleDeleteRewardChooseTier(ev);
        case "deletereward" -> handleDeleteReward(ev);
        case String s when s.startsWith("createreward_") -> handleCreateReward(ev);
        case String s when s.startsWith("choosereward_") -> handleChooseReward(ev);
        default -> e.reply("Unknown StringSelectInteractionEvent: %s".formatted(e.getComponentId()))
            .setEphemeral(true).queue();
      }
    } else if (e instanceof EntitySelectInteractionEvent ev) {
      switch (e.getComponentId()) {
        case "selfrole_add" -> handleConfigureSelfroleAdd(ev);
        case "selfrole_remove" -> handleConfigureSelfroleRemove(ev);
        default -> e.reply("Unknown EntitySelectInteractionEvent: %s".formatted(e.getComponentId()))
            .setEphemeral(true).queue();
      }
    } else {
      throw new IllegalArgumentException("Unknown event type: " + e.getClass().getName());
    }
  }

  private void handleConfigureSelfroleAdd(EntitySelectInteractionEvent e) {
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

  private void handleConfigureSelfroleRemove(EntitySelectInteractionEvent e) {
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

  private void handleTicketCreate(StringSelectInteractionEvent e) {
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

  private void handleTicketResolveBug(StringSelectInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var selected = e.getSelectedOptions().get(0).getValue();
      e.getMessage().delete().queue();

      if ("none".equals(selected)) {
        e.reply("No reward will be given out.").setEphemeral(true).queue();
        e.getChannel().asTextChannel().sendMessage("No reward will be given out.").queue();
        return;
      }

      try {
        var tierId = UUID.fromString(selected);
        var tierOpt = rewardTierRepository.findById(tierId);

        if (tierOpt.isEmpty()) {
          e.reply("Unknown reward tier, might've been deleted!").setEphemeral(true).queue();
          return;
        }

        var tier = tierOpt.get();
        var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
        var userId = ticket.getCreator();
        var user = e.getGuild().retrieveMemberById(userId).complete();

        var rewards = rewardRepository.findByTier(tier);

        if (rewards.isEmpty()) {
          e.reply("No rewards available for this tier!").setEphemeral(true).queue();
          return;
        }

        var menu = StringSelectMenu.create("choosereward_%s".formatted(userId))
            .setPlaceholder("Choose a Reward").addOption("None", "none");

        for (var reward : rewards) {
          menu.addOption(reward.getName(), reward.getId().toString());
        }

        e.getChannel().asTextChannel()
            .sendMessage("%s\nPlease choose a reward.".formatted(user.getAsMention()))
            .addActionRow(menu.build()).queue();

        e.reply("Reward tier to be given: `%s`".formatted(tier.getName())).setEphemeral(true).queue();
        e.getMessage().delete().queue();

      } catch (Exception ex) {
        log.error("Error in handleTicketResolveBug", ex);
        e.reply("An error occurred while processing the reward tier selection.").setEphemeral(true).queue();
      }
    }
  }

  private void handleCreateReward(StringSelectInteractionEvent e) {
    var guild = e.getGuild();
    var g = guildRepository.findById(guild.getIdLong()).orElse(new Guild());
    var member = e.getMember();
    var rewardId = UUID.fromString(e.getComponentId().substring(e.getComponentId().lastIndexOf('_') + 1));
    var rewardTierId = UUID.fromString(e.getSelectedOptions().get(0).getValue());

    var reward = rewardRepository.findById(rewardId);
    var rewardTier = rewardTierRepository.findById(rewardTierId);

    reward.ifPresent(r -> rewardRepository.save(r.setTier(rewardTier.get())));
    e.reply("Reward created!").setEphemeral(true).queue();

    if (g.getLog() != null) {
      messageUtil.sendLogMessage(
          "Command `%s` executed by `%s (%s)`\nCREATE BUG REWARD\n`%s` (Tier `%s`)".formatted(
              "/reward tier create",
              member.getEffectiveName(),
              member.getIdLong(),
              reward.get().getName(),
              rewardTier.get().getName()
          ), guild.getTextChannelById(g.getLog()));
    }
  }

  private void handleChooseReward(StringSelectInteractionEvent e) {
    var cid = e.getComponentId();
    var userId = cid.substring(cid.lastIndexOf('_') + 1);
    var user = e.getGuild().retrieveMemberById(userId).complete();
    if (e.getMember() != user) {
      e.reply("You are not permitted to choose a reward for this report!").setEphemeral(true).queue();
      return;
    }

    var selected = e.getSelectedOptions().get(0).getValue();
    if ("none".equals(selected)) {
      e.reply("You will not get a reward for this report.").setEphemeral(true).queue();
      e.getChannel().asTextChannel().sendMessage("The user chose not to get a reward.").queue();
      return;
    }

    var reward = rewardRepository.findById(UUID.fromString(selected)).orElse(null);
    if (reward == null) {
      e.reply("Unknown reward, might've been deleted!").setEphemeral(true).queue();
      return;
    }

    e.reply("You chose the reward **%s**".formatted(reward.getName())).setEphemeral(true).queue();
    e.getChannel().asTextChannel().sendMessage("Reward **%s** was chosen!".formatted(reward.getName()));
    e.getMessage().delete().queue();
  }

  private void handleDeleteRewardChooseTier(StringSelectInteractionEvent e) {
    var rewardTierId = UUID.fromString(e.getSelectedOptions().get(0).getValue());

    var menu = StringSelectMenu.create("deletereward")
        .setPlaceholder("Which Reward do you want to delete?");

    for (var reward : rewardRepository.findByTier(rewardTierRepository.findById(rewardTierId).get())) {
      menu.addOption(reward.getName(), reward.getId().toString());
    }

    if (menu.getOptions().isEmpty()) {
      e.reply("No rewards available for this tier!").setEphemeral(true).queue();
      return;
    }

    e.reply("").addActionRow(menu.build()).setEphemeral(true).queue();
    e.getMessage().delete().queue();
  }

  private void handleDeleteReward(StringSelectInteractionEvent e) {
    var guild = e.getGuild();
    var g = guildRepository.findById(guild.getIdLong()).orElse(new Guild());
    var member = e.getMember();

    var rewardId = UUID.fromString(e.getSelectedOptions().get(0).getValue());

    var reward = rewardRepository.findById(rewardId);
    var name = reward.get().getName();
    var tier = reward.get().getTier().getName();
    reward.ifPresent(rewardRepository::delete);

    e.reply("Reward deleted!").setEphemeral(true).queue();
    e.getMessage().delete().queue();
    if (g.getLog() != null) {
      messageUtil.sendLogMessage(
          "Command `%s` executed by `%s (%s)`\nDELETE BUG REWARD\n`%s` (Tier `%s`)".formatted(
              "/reward delete",
              member.getEffectiveName(),
              member.getIdLong(),
              name,
              tier
          ), guild.getTextChannelById(g.getLog()));
    }
  }
}
