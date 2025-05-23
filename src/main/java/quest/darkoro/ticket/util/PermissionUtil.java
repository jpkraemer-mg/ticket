package quest.darkoro.ticket.util;

import static net.dv8tion.jda.api.Permission.CREATE_PRIVATE_THREADS;
import static net.dv8tion.jda.api.Permission.CREATE_PUBLIC_THREADS;
import static net.dv8tion.jda.api.Permission.MANAGE_CHANNEL;
import static net.dv8tion.jda.api.Permission.MESSAGE_ATTACH_FILES;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.MESSAGE_MENTION_EVERYONE;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND_POLLS;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.persistence.AdministratorRepository;
import quest.darkoro.ticket.persistence.CategoryRepository;

@Component
@RequiredArgsConstructor
public class PermissionUtil {

  private final AdministratorRepository administratorRepository;
  private final JDA bot;
  private final CategoryRepository categoryRepository;

  public boolean isPermitted(SlashCommandInteractionEvent e, Long gid, Member member) {
    boolean hasPermission = member.hasPermission(Permission.ADMINISTRATOR);

    if (administratorRepository.getAllByGuildId(gid).isEmpty() && !hasPermission) {
      e.reply("You must be an administrator to use this command.").setEphemeral(true).queue();
      return hasPermission;
    }

    var adminRoles = administratorRepository.getAllByGuildId(gid);
    StringBuilder sb = new StringBuilder();

    if (!hasPermission) {
      for (var adminRole : adminRoles) {
        var role = e.getGuild().getRoleById(adminRole.getRoleId());
        sb.append(role.getAsMention()).append("\n");
        if (member.getRoles().contains(role)) {
          hasPermission = true;
          break;
        }
      }
    }

    if (!hasPermission) {
      e.reply("You must have one of the following roles or Administrator permission for this command:\n%s"
              .formatted(sb))
          .setEphemeral(true)
          .queue();
      return hasPermission;
    }
    return hasPermission;
  }

  public boolean isPermitted(ButtonInteractionEvent e, Long gid, Member member) {
    boolean hasPermission = member.hasPermission(Permission.ADMINISTRATOR);

    if (administratorRepository.getAllByGuildId(gid).isEmpty() && !hasPermission) {
      e.reply("You must be an administrator to use this button.").setEphemeral(true).queue();
      return hasPermission;
    }

    var adminRoles = administratorRepository.getAllByGuildId(gid);
    StringBuilder sb = new StringBuilder();

    if (!hasPermission) {
      for (var adminRole : adminRoles) {
        var role = e.getGuild().getRoleById(adminRole.getRoleId());
        sb.append(role.getAsMention()).append("\n");
        if (member.getRoles().contains(role)) {
          hasPermission = true;
          break;
        }
      }
    }

    if (!hasPermission) {
      e.reply("You must have one of the following roles or Administrator permission for this button:\n%s"
              .formatted(sb))
          .setEphemeral(true)
          .queue();
      return hasPermission;
    }
    return hasPermission;
  }

  public boolean isPermitted(StringSelectInteractionEvent e, Long gid, Member member) {
    boolean hasPermission = member.hasPermission(Permission.ADMINISTRATOR);

    if (administratorRepository.getAllByGuildId(gid).isEmpty() && !hasPermission) {
      e.reply("You must be an administrator to use this select menu.").setEphemeral(true).queue();
      return hasPermission;
    }

    var adminRoles = administratorRepository.getAllByGuildId(gid);
    StringBuilder sb = new StringBuilder();

    if (!hasPermission) {
      for (var adminRole : adminRoles) {
        var role = e.getGuild().getRoleById(adminRole.getRoleId());
        sb.append(role.getAsMention()).append("\n");
        if (member.getRoles().contains(role)) {
          hasPermission = true;
          break;
        }
      }
    }

    if (!hasPermission) {
      e.reply("You must have one of the following roles or Administrator permission for this select menu:\n%s"
              .formatted(sb))
          .setEphemeral(true)
          .queue();
      return hasPermission;
    }
    return hasPermission;
  }

  public EnumSet<Permission> getAllow() {
    return EnumSet.of(VIEW_CHANNEL, MESSAGE_SEND, MESSAGE_HISTORY, MESSAGE_ATTACH_FILES,
        MESSAGE_EMBED_LINKS);
  }

  public EnumSet<Permission> getDeny() {
    return EnumSet.of(VIEW_CHANNEL, MESSAGE_SEND, MESSAGE_HISTORY, MANAGE_CHANNEL,
        CREATE_PRIVATE_THREADS, CREATE_PUBLIC_THREADS, MESSAGE_MENTION_EVERYONE, MESSAGE_SEND_POLLS,
        MESSAGE_EMBED_LINKS, MESSAGE_ATTACH_FILES);
  }

  public EnumSet<Permission> getFilteredDeny() {
    var deny = getDeny();
    deny.removeAll(getAllow());
    return deny;
  }

  public EnumSet<Permission> getBotPermissions() {
    return getDeny();
  }

  public List<Role> getRoles(Long gid) {
    return administratorRepository.getAllByGuildId(gid)
        .stream()
        .map(a ->
            bot.getGuildById(gid) != null ? bot.getGuildById(gid).getRoleById(a.getRoleId()) : null)
        .filter(Objects::nonNull)
        .toList();
  }

  public boolean validCategory(Long gid, Long cid) {
    return bot.getGuildById(gid).getCategoryById(cid) == null ||
        (bot.getGuildById(gid).getCategoryById(cid) != null &&
            categoryRepository.findByGuildId(gid).stream().anyMatch(c -> c.getId().equals(cid)));
  }
}
