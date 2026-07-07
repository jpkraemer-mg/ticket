package quest.darkoro.ticket.services;

import static net.dv8tion.jda.api.entities.channel.ChannelType.CATEGORY;
import static net.dv8tion.jda.api.entities.channel.ChannelType.TEXT;
import static net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Category;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.CategoryRepository;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigureCommandService {

  private final PermissionUtil permissionUtil;
  private final CategoryRepository categoryRepository;
  private final MessageUtil messageUtil;
  private final GuildRepository guildRepository;

  public void handleConfigureCategoryAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var guild = e.getGuild();

      var roles = Stream.of("role1", "role2", "role3", "role4")
          .map(e::getOption)
          .filter(Objects::nonNull)
          .map(OptionMapping::getAsRole)
          .toList();

      var mentions = roles.stream().map(IMentionable::getAsMention)
          .collect(Collectors.joining(", "));

      roles = Stream.concat(roles.stream(), permissionUtil.getRoles(gid).stream()).distinct()
          .toList();

      if (categoryRepository.findByNameAndGuildId(e.getOption("name").getAsString(), gid)
          .isPresent()) {
        e.reply("Category with that name already exists").setEphemeral(true).queue();
        return;
      }

      var category = guild.createCategory(e.getOption("name").getAsString());
      var open = e.getOption("open") != null && e.getOption("open").getAsBoolean();
      if (!open) {
        roles.forEach(
            r -> category.addRolePermissionOverride(r.getIdLong(), permissionUtil.getAllow(),
                permissionUtil.getFilteredDeny()));
        category.addRolePermissionOverride(guild.getBotRole().getIdLong(),
            permissionUtil.getBotPermissions(), null);
        category.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null,
            permissionUtil.getDeny());
      }
      e.reply("Category added with roles\n%s".formatted(mentions)).setEphemeral(true).queue();
      var assignedRoles = roles;
      category.queue(completeCategory -> {
        categoryRepository.save(new Category()
            .setId(completeCategory.getIdLong())
            .setDescription(e.getOption("description").getAsString())
            .setGuildId(gid)
            .setName(e.getOption("name").getAsString())
            .setMentions(mentions)
        );
        messageUtil.refreshTicketMessage(guild, e.getJDA());
        messageUtil.sendCommandLog(member, "/configure category add",
            "CATEGORY CREATE `%s (%s)`\n`%s`".formatted(
                completeCategory.getName(),
                completeCategory.getIdLong(),
                !assignedRoles.isEmpty() ? assignedRoles.stream().map(Role::getName)
                    .collect(Collectors.joining(", ")) : "No roles assigned to category"));
      }, error -> log.error("Failed to create category", error));
    }
  }

  public void handleConfigureCategoryRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("category").getAsChannel();
      if (channel.getType() != ChannelType.CATEGORY) {
        e.reply("You may only select a **category** type channel!").setEphemeral(true).queue();
        return;
      }

      var category = categoryRepository.findById(channel.getIdLong()).orElse(null);

      if (category == null) {
        e.reply("This category is not a configured ticket category!").setEphemeral(true).queue();
        return;
      }
      var name = channel.getName();
      channel.delete().queue();
      categoryRepository.delete(category);
      e.reply("Category %s removed".formatted(name)).setEphemeral(true).queue();
      messageUtil.refreshTicketMessage(e.getGuild(), e.getJDA());
      messageUtil.sendCommandLog(member, "/configure category remove",
          "CATEGORY REMOVE: `%s (%s)`".formatted(name, category.getId()));
    }
  }

  public void handleConfigureChannelLog(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("log").getAsChannel();
      if (channel.getType() != TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setLog(channel.getIdLong()));
      e.reply("Log channel set to %s".formatted(channel.getAsMention())).setEphemeral(true).queue();
    }
  }

  public void handleConfigureChannelRole(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("roles").getAsChannel();
      if (channel.getType() != ChannelType.TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setRole(channel.getIdLong()));
      e.reply("The channel from which to self-assign roles has been set to %s".formatted(
          channel.getAsMention())).setEphemeral(true).queue();
      messageUtil.sendRoleMessage(channel.asTextChannel(), e.getJDA());
      messageUtil.sendCommandLog(member, "/configure channel role",
          "CONFIGURE SELF-ASSIGNABLE ROLE CHANNEL: `%s (%s)`".formatted(
              channel.getName(), channel.getId()));
    }
  }

  public void handleConfigureChannelSetup(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();

    if (!permissionUtil.isPermitted(e, gid, member)) {
      return;
    }
    var setupCategory = e.getOption("category").getAsChannel();
    if (setupCategory.getType() != CATEGORY) {
      e.reply("You may only select a **category** type channel to use for setup!")
          .setEphemeral(true).queue();
      return;
    }
    e.deferReply(true).queue();

    var g = guildRepository.findById(gid).orElse(new Guild().setId(gid));
    var setupCat = setupCategory.asCategory();
    var logging = e.getGuild().createTextChannel("command-logging").setParent(setupCat);
    var ticket = e.getGuild().createTextChannel("create-a-ticket").setParent(setupCat);
    var transcript = e.getGuild().createTextChannel("ticket-transcripts").setParent(setupCat);
    var selfRoles = e.getGuild().createTextChannel("self-assign-roles").setParent(setupCat);
    List<Role> roles = permissionUtil.getRoles(gid);
    List.of(logging, ticket, transcript, selfRoles)
        .forEach(c -> {
          roles.forEach(
              r -> c.addRolePermissionOverride(r.getIdLong(), permissionUtil.getAllow(), null)
          );
          c.addRolePermissionOverride(e.getGuild().getBotRole().getIdLong(),
              permissionUtil.getBotPermissions(), null);
          c.addRolePermissionOverride(e.getGuild().getPublicRole().getIdLong(), null,
              permissionUtil.getDeny());
        });
    RestAction.allOf(logging, ticket, transcript, selfRoles).queue(channels -> {
      var logDone = channels.get(0);
      var ticketDone = channels.get(1);
      var transcriptDone = channels.get(2);
      var selfRolesDone = channels.get(3);
      guildRepository.save(g.setBase(ticketDone.getIdLong()).setLog(logDone.getIdLong()).setRole(
          selfRolesDone.getIdLong()).setTranscript(transcriptDone.getIdLong()));
      messageUtil.sendTicketMessage(ticketDone, e.getJDA());
      messageUtil.sendRoleMessage(selfRolesDone, e.getJDA());
      messageUtil.sendLogMessage(
          "Channel setup executed by `%s (%s)`".formatted(member.getEffectiveName(),
              member.getIdLong()), logDone);
      e.getHook().sendMessage("Channel setup complete!").queue();
    }, error -> {
      log.error("Failed to create setup channels", error);
      e.getHook().sendMessage("Failed to create the setup channels!").queue();
    });
  }

  public void handleConfigureChannelTicket(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("channel").getAsChannel();
      if (channel.getType() != ChannelType.TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setBase(channel.getIdLong()));
      messageUtil.sendTicketMessage(channel.asTextChannel(), e.getJDA());
      e.reply("Tickets may now be created from %s".formatted(channel.getAsMention()))
          .setEphemeral(true).queue();
      messageUtil.sendCommandLog(member, "/configure channel ticket",
          "CONFIGURE TICKET MESSAGE CHANNEL: `%s (%s)`".formatted(
              channel.getName(), channel.getId()));
    }
  }

  public void handleConfigureChannelTranscript(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("transcript").getAsChannel();
      if (channel.getType() != ChannelType.TEXT) {
        e.reply("You may only select a **text** channel!").setEphemeral(true).queue();
        return;
      }
      var guild = guildRepository.findById(gid).orElse(new Guild());
      guildRepository.save(guild.setId(gid).setTranscript(channel.getIdLong()));
      e.reply("Transcript channel set to %s".formatted(channel.getAsMention())).setEphemeral(true)
          .queue();
      messageUtil.sendCommandLog(member, "/configure channel transcript",
          "CONFIGURE TRANSCRIPT CHANNEL: `%s (%s)`".formatted(
              channel.getName(), channel.getId()));
    }
  }

  public void handleSelfroleAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var menu = EntitySelectMenu.create("selfrole_add", ROLE)
          .setPlaceholder("Select roles to be self-assignable")
          .setMinValues(1)
          .setMaxValues(10)
          .build();
      e.reply("")
          .setActionRow(menu)
          .setEphemeral(true)
          .queue();
    }
  }

  public void handleSelfroleRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var menu = EntitySelectMenu.create("selfrole_remove", ROLE)
          .setPlaceholder("Select roles that should no longer be self-assignable")
          .setMinValues(1)
          .setMaxValues(10)
          .build();
      e.reply("")
          .setActionRow(menu)
          .setEphemeral(true)
          .queue();
    }
  }
}
