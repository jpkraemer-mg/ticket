package quest.darkoro.ticket.listener.secondary.command.ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@RequiredArgsConstructor
@Slf4j
public class TicketRoleRemoveListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") ||
        !"role".equals(e.getSubcommandGroup()) || !"remove".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          var role = e.getOption("role").getAsRole();
          e.getChannel().asTextChannel().getManager().removePermissionOverride(role.getIdLong())
              .queue();
          e.reply("Role %s removed from ticket".formatted(role.getAsMention())).setEphemeral(true)
              .queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }
}
