package quest.darkoro.ticket.services;

import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;
import static net.dv8tion.jda.api.entities.channel.ChannelType.CATEGORY;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Administrator;
import quest.darkoro.ticket.persistence.repository.AdministratorRepository;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCommandService {

  private final PermissionUtil permissionUtil;
  private final AdministratorRepository administratorRepository;
  private final GuildRepository guildRepository;
  private final CategoryRepository categoryRepository;
  private final MessageUtil messageUtil;

  public void handleTicketAdminsAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var role = e.getOption("role").getAsRole();
      if (role.equals(e.getGuild().getBotRole())) {
        e.reply("Adding the bot role to ticket administrators is not supported!").setEphemeral(true)
            .queue();
        return;
      }
      administratorRepository.save(
          new Administrator().setGuildId(gid).setRoleId(role.getIdLong()).setName(role.getName()));
      e.reply("Role %s added to ticket admins.".formatted(role.getAsMention())).setEphemeral(true)
          .queue();

      var guild = guildRepository.findById(gid).orElse(null);
      categoryRepository.findByGuildId(gid).forEach(c -> {
        var category = e.getGuild().getCategoryById(c.getId());
        category.getTextChannels().forEach(t -> t.getManager()
            .putRolePermissionOverride(role.getIdLong(), permissionUtil.getAllow(),
                permissionUtil.getFilteredDeny()).queue());
        category.getManager().putRolePermissionOverride(role.getIdLong(), permissionUtil.getAllow(),
            permissionUtil.getFilteredDeny()).queue();
      });
      if (guild != null) {
        if (guild.getLog() != null) {
          messageUtil.sendLogMessage(
              "Command `%s` executed by `%s (%s)`\nADD TICKET ADMIN\n`%s (%s)`".formatted(
                  "/ticket admins add",
                  member.getEffectiveName(),
                  member.getIdLong(),
                  role.getName(),
                  role.getId()), e.getGuild().getTextChannelById(guild.getLog())
          );
        }
      }
    }
  }

  public void handleTicketAdminsRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var role = e.getOption("role").getAsRole();
      if (role.equals(e.getGuild().getBotRole())) {
        e.reply("Removing the bot role from ticket administrators is not supported!")
            .setEphemeral(true).queue();
        return;
      }
      administratorRepository.removeByRoleId(role.getIdLong());
      e.reply("Role %s removed from ticket admins.".formatted(role.getAsMention()))
          .setEphemeral(true).queue();

      var guild = guildRepository.findById(gid).orElse(null);
      categoryRepository.findByGuildId(gid).forEach(c -> {
        var category = e.getGuild().getCategoryById(c.getId());
        category.getTextChannels()
            .forEach(t -> t.getManager().removePermissionOverride(role).queue());
        category.getManager().removePermissionOverride(role).queue();
      });
      if (guild != null) {
        if (guild.getLog() != null) {
          messageUtil.sendLogMessage(
              "Command `%s` executed by `%s (%s)`\nREMOVE TICKET ADMIN\n`%s (%s)`".formatted(
                  "/ticket admins remove",
                  member.getEffectiveName(),
                  member.getIdLong(),
                  role.getName(),
                  role.getId()), e.getGuild().getTextChannelById(guild.getLog())
          );
        }
      }
    }
  }

  public void handleTicketMove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var category = e.getOption("category").getAsChannel();
      var manager = e.getChannel().asTextChannel().getManager();
      if (!category.getType().equals(CATEGORY)) {
        e.reply("You must select a **category** type channel to move this ticket to!")
            .setEphemeral(true).queue();
        return;
      }
      manager.setParent(category.asCategory()).queue();
      e.reply("Ticket moved to `%s` by `%s (%s)`".formatted(category.getName(),
          member.getEffectiveName(), member.getIdLong())).queue();
    }
  }

  public void handleTicketRename(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    boolean isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(e.getGuild().getIdLong(), e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      e.getChannel().asTextChannel().getManager().setName(e.getOption("new_name").getAsString())
          .queue();
      e.reply("Ticket renamed to `%s` by `%s (%s)`".formatted(e.getOption("new_name").getAsString(),
          member.getEffectiveName(), member.getIdLong())).queue();
    }
  }

  public void handleTicketRoleAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var role = e.getOption("role").getAsRole();
      if (role.equals(e.getGuild().getBotRole())) {
        e.reply("Adding the bot role to a ticket is not supported!")
            .setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          e.getChannel().asTextChannel().getManager().putRolePermissionOverride(role.getIdLong(),
              permissionUtil.getAllow(), permissionUtil.getFilteredDeny()).queue();
          e.reply("Role `%s` added to ticket by `%s (%s)`".formatted(role.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }

  public void handleTicketRoleRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var role = e.getOption("role").getAsRole();
      if (role.equals(e.getGuild().getBotRole())) {
        e.reply("Removing the bot role from a ticket is not supported!")
            .setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          e.getChannel().asTextChannel().getManager().removePermissionOverride(role.getIdLong())
              .queue();
          e.reply("Role `%s` removed from ticket by `%s (%s)`".formatted(role.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }

  public void handleTicketUserAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var user = e.getOption("user").getAsMember();
      if (user.equals(e.getGuild().getSelfMember())) {
        e.reply("Adding the bot user to a ticket is not supported!")
            .setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          e.getChannel().asTextChannel().getManager().putMemberPermissionOverride(user.getIdLong(),
              List.of(MESSAGE_SEND, VIEW_CHANNEL, MESSAGE_HISTORY), new ArrayList<>()).queue();
          e.reply("User `%s` added to ticket by `%s (%s)`".formatted(user.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }

  public void handleTicketUserRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var user = e.getOption("user").getAsMember();
      if (user.equals(e.getGuild().getSelfMember())) {
        e.reply("Removing the bot user from a ticket is not supported!")
            .setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          e.getChannel().asTextChannel().getManager().removePermissionOverride(user.getIdLong())
              .queue();
          e.reply("User `%s` removed from ticket by `%s (%s)`".formatted(user.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }
}
