package quest.darkoro.ticket.listener.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Administrator;
import quest.darkoro.ticket.persistence.repository.AdministratorRepository;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@RequiredArgsConstructor
@Slf4j
public class TicketAdminsAddCommandListener extends ListenerAdapter {

  private final AdministratorRepository administratorRepository;
  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;
  private final CategoryRepository categoryRepository;

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") ||
        !"admins".equals(e.getSubcommandGroup()) || !"add".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var role = e.getOption("role").getAsRole();
      if (role.equals(e.getGuild().getBotRole())) {
        e.reply("Adding the bot role to ticket administrators is not supported!").setEphemeral(true).queue();
        return;
      }
      administratorRepository.save(
          new Administrator().setGuildId(gid).setRoleId(role.getIdLong()).setName(role.getName()));
      e.reply("Role %s added to ticket admins.".formatted(role.getAsMention())).setEphemeral(true)
          .queue();

      var guild = guildRepository.findById(gid).orElse(null);
      categoryRepository.findByGuildId(gid).forEach(c -> {
        var category = e.getGuild().getCategoryById(c.getId());
        category.getTextChannels().forEach(t -> t.getManager().putRolePermissionOverride(role.getIdLong(), permissionUtil.getAllow(), permissionUtil.getFilteredDeny()).queue());
        category.getManager().putRolePermissionOverride(role.getIdLong(), permissionUtil.getAllow(), permissionUtil.getFilteredDeny()).queue();
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
}
